package org.rockyang.blockchain.P2P;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.testing.FakeTxBuilder;
import java.io.Serializable;
import java.util.Date;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;


public class NIOSocketClientTest {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        socketChannel.connect(new InetSocketAddress("192.168.0.11", 9999));
        // 完成套接字通道的连接过程
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
        String a =tx.toString();
        if (socketChannel.finishConnect()) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(a.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        socketChannel.close();
    }

}