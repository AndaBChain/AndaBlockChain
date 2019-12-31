package org.rockyang.blockchain.nio.Runnable.test;

import com.google.gson.Gson;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Serializable.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 写操作工具类
 * @author Wang HaiTian
 *
 */
public class ResponeProcessorC {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = UnitTestParams.get();
    //构造线程池
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void ProcessorRespone(final SelectionKey key) {
        //拿到线程并执行
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // 写操作
                    SocketChannel writeChannel = (SocketChannel) key.channel();
                    //拿到客户端传递的数据
                    ByteArrayOutputStream attachment = (ByteArrayOutputStream)key.attachment();

                    System.out.println("客户端发送来的数据："+new String(attachment.toByteArray()));

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    String message = "你好，我好，大家好！！";
                    buffer.put(message.getBytes());
                    buffer.flip();
                    writeChannel.write(buffer);
                    writeChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
