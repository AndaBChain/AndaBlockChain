package com.onets.core.network.Nio;

import android.util.Log;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.onets.wallet.Constants;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitcoinj.net.ClientConnectionManager;
import org.bitcoinj.net.StreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which manages a set of client connections. Uses Java NIO to select network events and processes them in a
 * single network processing thread.
 */
public class NioClientManager extends AbstractExecutionThreadService implements ClientConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(NioClientManager.class);
    private static final String TAG = "NioClientManager";
    private final Selector selector;//选择器

    /**
     * 套接字通道和解析器
     */
    class SocketChannelAndParser {
        SocketChannel sc;
        StreamParser parser;

        SocketChannelAndParser(SocketChannel sc, StreamParser parser) {
            this.sc = sc;
            this.parser = parser;
        }
    }

    final Queue<SocketChannelAndParser> newConnectionChannels = new LinkedBlockingQueue();//连接通道队列
    private final Set<ConnectionHandler> connectedHandlers = Collections.synchronizedSet(new HashSet());//链接处理程序

    /**
     *
     * @param key 选择键
     * @throws IOException
     */
    private void handleKey(SelectionKey key) throws IOException {
        if ((key.isValid()) && (key.isConnectable())) {
            StreamParser parser = (StreamParser) key.attachment();
            SocketChannel sc = (SocketChannel) key.channel();
            ConnectionHandler handler = new ConnectionHandler(parser, key, this.connectedHandlers);
            try {
                if (sc.finishConnect()) {
                    log.info("Successfully connected to {}", sc.socket().getRemoteSocketAddress());
                    Log.d(TAG, Constants.LOG_LABLE + "handleKey: Successfully connected to " + sc.socket().getRemoteSocketAddress());
                    key.interestOps((key.interestOps() | 0x1) & 0xFFFFFFF7).attach(handler);
                    handler.parser.connectionOpened();
                } else {
                    log.error("Failed to connect to {}", sc.socket().getRemoteSocketAddress());
                    Log.d(TAG, Constants.LOG_LABLE + "handleKey: Failed connected to " + sc.socket().getRemoteSocketAddress());
                    handler.closeConnection();
                }
            } catch (Exception e) {
                Throwable cause = Throwables.getRootCause(e);
                log.error("Failed to connect with exception: {}: {}", cause.getClass().getName(), cause.getMessage());
                handler.closeConnection();
            }
        } else {
            ConnectionHandler.handleKey(key);
        }
    }

    /**
     * 创建使用Java NIO进行套接字管理的新客户端管理器。
     * 使用一个线程处理所有select调用
     */
    public NioClientManager() {
        try {
            this.selector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        SelectionKey key;//选择键
        Iterator localIterator;//迭代器
        try {
            Thread.currentThread().setPriority(1);
            SocketChannelAndParser conn;
            while (isRunning()) {
                while ((conn = (SocketChannelAndParser) this.newConnectionChannels.poll()) != null) {
                    try {
                        key = conn.sc.register(this.selector, 8);
                        key.attach(conn.parser);
                    } catch (ClosedChannelException e) {
                        log.info("SocketChannel was closed before it could be registered");
                    }
                }
                this.selector.select();

                Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    key = (SelectionKey) keyIterator.next();
                    keyIterator.remove();
                    handleKey(key);
                }
            }
            return;
        } catch (Exception e) {
            log.error("Error trying to open/read from connection: ", e);
        } finally {
            for (localIterator = this.selector.keys().iterator(); localIterator.hasNext(); ) {
                key = (SelectionKey) localIterator.next();
                try {
                    key.channel().close();
                } catch (IOException e) {
                    log.error("Error closing channel", e);
                }
                key.cancel();
                if ((key.attachment() instanceof ConnectionHandler)) {
                    ConnectionHandler.handleKey(key);
                }
            }
            try {
                this.selector.close();
            } catch (IOException e) {
                log.error("Error closing client manager selector", e);
            }
        }
    }

    /**
     * 使用用于处理传入数据的给定解析器创建到给定地址的新连接。
     * @param serverAddress
     * @param parser
     */
    public void openConnection(SocketAddress serverAddress, StreamParser parser) {
        if (!isRunning()) {
            throw new IllegalStateException();
        }
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(serverAddress);
            this.newConnectionChannels.offer(new SocketChannelAndParser(sc, parser));
            this.selector.wakeup();
        } catch (IOException e) {
            log.error("Could not connect to " + serverAddress);
            throw new RuntimeException(e);
        } catch (AssertionError e) {
            log.error("Could not connect to " + serverAddress);
            throw new RuntimeException(e);
        }
    }

    public void triggerShutdown() {
        this.selector.wakeup();
    }

    /**
     * 获取已连接的对等点的数量
     * @return
     */
    public int getConnectedClientCount() {
        return this.connectedHandlers.size();
    }

    /**
     * Closes n peer connections
     * @param n
     */
    public void closeConnections(int n) {
        while (n-- > 0) {
            ConnectionHandler handler;
            synchronized (this.connectedHandlers) {
                handler = (ConnectionHandler) this.connectedHandlers.iterator().next();
            }
            if (handler != null) {
                handler.closeConnection();
            }
        }
    }
}
