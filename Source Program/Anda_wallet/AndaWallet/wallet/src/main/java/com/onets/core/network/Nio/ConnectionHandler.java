package com.onets.core.network.Nio;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import org.bitcoinj.net.MessageWriteTarget;
import org.bitcoinj.net.StreamParser;
import org.bitcoinj.net.StreamParserFactory;
import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * 连接处理器
 */
class ConnectionHandler implements MessageWriteTarget {
    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final int BUFFER_SIZE_LOWER_BOUND = 4096;//缓存区大小下限
    private static final int BUFFER_SIZE_UPPER_BOUND = 65536;//缓存区大小上限
    private static final int OUTBOUND_BUFFER_BYTE_COUNT = 33554456;//出站缓存区字节数
    private final ReentrantLock lock = Threading.lock("nioConnectionHandler");
    @GuardedBy("lock")
    private final ByteBuffer readBuff;
    @GuardedBy("lock")
    private final SocketChannel channel;
    @GuardedBy("lock")
    private final SelectionKey key;
    @GuardedBy("lock")
    StreamParser parser;
    @GuardedBy("lock")
    private boolean closeCalled = false;
    @GuardedBy("lock")
    private long bytesToWriteRemaining = 0L;
    @GuardedBy("lock")
    private final LinkedList<ByteBuffer> bytesToWrite = new LinkedList();//写入缓存
    private Set<ConnectionHandler> connectedHandlers;

    public ConnectionHandler(StreamParserFactory parserFactory, SelectionKey key) throws IOException {
        this(parserFactory.getNewParser(((SocketChannel) key.channel()).socket().getInetAddress(), ((SocketChannel) key.channel()).socket().getPort()), key);
        if (this.parser == null) {
            throw new IOException("Parser factory.getNewParser returned null");
        }
    }

    private ConnectionHandler(@Nullable StreamParser parser, SelectionKey key) {
        this.key = key;
        this.channel = ((SocketChannel) Preconditions.checkNotNull((SocketChannel) key.channel()));
        if (parser == null) {
            this.readBuff = null;
            return;
        }
        this.parser = parser;
        this.readBuff = ByteBuffer.allocateDirect(Math.min(Math.max(parser.getMaxMessageSize(), 4096), 65536));
        parser.setWriteTarget(this);
        this.connectedHandlers = null;
    }

    public ConnectionHandler(StreamParser parser, SelectionKey key, Set<ConnectionHandler> connectedHandlers) {
        this((StreamParser) Preconditions.checkNotNull(parser), key);

        this.lock.lock();
        boolean alreadyClosed = false;
        try {
            alreadyClosed = this.closeCalled;
            this.connectedHandlers = connectedHandlers;
            if (!alreadyClosed) {
                Preconditions.checkState(connectedHandlers.add(this));
            }
        } finally {
            this.lock.unlock();
        }
    }

    @GuardedBy("lock")
    private void setWriteOps() {
        this.key.interestOps(this.key.interestOps() | 0x4);

        this.key.selector().wakeup();
    }

    /* Error */
    private void tryWriteBytes() throws IOException {

    }

    public void writeBytes(byte[] message) throws IOException {
        this.lock.lock();
        try {
            if (this.bytesToWriteRemaining + message.length > 33554456L) {
                throw new IOException("Outbound buffer overflowed");
            }
            this.bytesToWrite.offer(ByteBuffer.wrap(Arrays.copyOf(message, message.length)));
            this.bytesToWriteRemaining += message.length;
            setWriteOps();
        } catch (IOException e) {
            this.lock.unlock();
            log.error("Error writing message to connection, closing connection", e);
            closeConnection();
            throw e;
        } catch (CancelledKeyException e) {
            this.lock.unlock();
            log.error("Error writing message to connection, closing connection", e);
            closeConnection();
            throw new IOException(e);
        }
        this.lock.unlock();
    }

    public void closeConnection() {
        try {
            this.channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connectionClosed();
    }

    /* Error */
    private void connectionClosed() {

    }

    public static void handleKey(SelectionKey key) {
        ConnectionHandler handler = (ConnectionHandler) key.attachment();
        try {
            if (handler == null) {
                return;
            }
            if (!key.isValid()) {
                handler.closeConnection();
                return;
            }
            if (key.isReadable()) {
                int read = handler.channel.read(handler.readBuff);
                if (read == 0) {
                    return;
                }
                if (read == -1) {
                    key.cancel();
                    handler.closeConnection();
                    return;
                }
                handler.readBuff.flip();

                int bytesConsumed = ((StreamParser) Preconditions.checkNotNull(handler.parser)).receiveBytes(handler.readBuff);
                Preconditions.checkState(handler.readBuff.position() == bytesConsumed);

                handler.readBuff.compact();
            }
            if (key.isWritable()) {
                handler.tryWriteBytes();
            }
        } catch (Exception e) {
            log.error("Error handling SelectionKey: {}", Throwables.getRootCause(e).getMessage());
            handler.closeConnection();
        }
    }
}
