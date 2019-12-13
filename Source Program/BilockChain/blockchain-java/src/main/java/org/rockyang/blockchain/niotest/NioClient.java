package org.rockyang.blockchain.niotest;

import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.core.BitcoinSerializer;
import org.rockyang.blockchain.testing.FakeTxBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 */
public class NioClient {
    private static final String host = "127.0.0.1";
    private static final int port = 8088;
    private Selector selector;
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = MainNetParams.get();
    public  void  validation(String ip,Transaction tx){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NioClient client = new NioClient();
                client.connect(ip, port);
                try {
                    client.listen(tx);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void connect(String host, int port) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            this.selector = Selector.open();
            sc.register(selector, SelectionKey.OP_CONNECT);
            sc.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(Transaction tx) throws IOException {
        CharSequence cs ="wanghaitian";
        Transaction tx1 = FakeTxBuilder.createFakeTx(UNITTEST);
        BitcoinSerializer deserialize = new BitcoinSerializer(params,true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Message message = tx;
        deserialize.serialize(message,out);
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
                                ByteBuffer buffer = ByteBuffer.allocate(4096);
                                String h ="hello";
                                buffer.put(h.getBytes());
                                buffer.flip();
                                socketChannel.write(buffer);
                            } else if (selectionKey.isReadable()) {
                                SocketChannel sc = (SocketChannel) selectionKey.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(4096);
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
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/

    }
}
