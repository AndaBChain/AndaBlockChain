package com.onets.core.network.Nio;

import android.util.Log;

import com.onets.wallet.Constants;

import org.bitcoinj.testing.FakeTxBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOSocketClientTest {
    private static final String TAG = "NIOSocketClientTest";
    private static final String host = "192.168.0.11";
    private static final int port = 8080;
    private Selector selector;

    public static void NIOSCTest(String tx) {
        Log.d(TAG, Constants.LOG_LABLE + "NIOSCTest: " + tx);
        NIOSocketClientTest client = new NIOSocketClientTest();
        client.connect(host, port);
        client.listen(tx);
    }

    public void connect(String host, int port) {
        try {
            SocketChannel sc = SocketChannel.open();
            Log.d(TAG, Constants.LOG_LABLE + "connect: SCopen " + sc.isOpen());
            sc.configureBlocking(false);
            this.selector = Selector.open();
            Log.d(TAG, Constants.LOG_LABLE + "connect: Sopen " + this.selector.isOpen());
            sc.register(selector, SelectionKey.OP_CONNECT);
            Log.d(TAG, Constants.LOG_LABLE + "connect: " + new InetSocketAddress(host, port));
            sc.connect(new InetSocketAddress(host, port));
            Log.d(TAG, Constants.LOG_LABLE + "connect: " + sc.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(String tx) {
        String a =tx.toString();
        while (true) {
            try {
                int events = selector.select();
                if (events > 0) {
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey selectionKey = selectionKeys.next();
                        selectionKeys.remove();
                        //连接事件
                        if (selectionKey.isConnectable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            if (socketChannel.isConnectionPending()) {
                                socketChannel.finishConnect();
                            }

                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            ByteBuffer buffer = ByteBuffer.allocate(2048);
                            buffer.put(a.getBytes());
                            buffer.flip();
                            socketChannel.write(buffer);
                        } else if (selectionKey.isReadable()) {
                            SocketChannel sc = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(2048);
                            sc.read(buffer);
                            buffer.flip();
                            System.out.println(new String(buffer.array()));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
