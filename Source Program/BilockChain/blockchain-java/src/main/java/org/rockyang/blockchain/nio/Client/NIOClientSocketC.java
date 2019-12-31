package org.rockyang.blockchain.nio.Client;

import com.google.gson.Gson;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Serializable.Peer;
import org.rockyang.blockchain.testing.FakeTxBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 *  NIO 客户端
 *  @author Wang HaiTian
 */
public class NIOClientSocketC {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = UnitTestParams.get();
    public static void main(String[] args) throws IOException {
        //使用线程模拟用户 并发访问
            new Thread(){
                public void run() {
                    try {
                        //1.创建SocketChannel
                        SocketChannel socketChannel=SocketChannel.open();
                        //2.连接服务器
                        socketChannel.connect(new InetSocketAddress("192.168.0.11",8989));
                        //写数据
                        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
                        BitcoinSerializer deserialize = new BitcoinSerializer(params,true);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Message message = tx;
                        deserialize.serialize(message,out);
                        String msg="qianbao";
                        ByteBuffer buffer=ByteBuffer.allocate(1024);
                        buffer.put(out.toByteArray());
                        buffer.flip();
                        socketChannel.write(buffer);
                        socketChannel.shutdownOutput();
                        //读数据
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int len = 0;
                        while (true) {
                            buffer.clear();
                            len = socketChannel.read(buffer);
                            if (len == -1)
                                break;
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                bos.write(buffer.get());
                            }
                        }
                        System.out.println("客户端收到:"+new String(bos.toByteArray()));
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }.start();
    }
}
