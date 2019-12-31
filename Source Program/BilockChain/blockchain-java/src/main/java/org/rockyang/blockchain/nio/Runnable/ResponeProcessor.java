package org.rockyang.blockchain.nio.Runnable;

import com.google.gson.Gson;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Serializable.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 写操作工具类
 * @author Wang HaiTian
 *
 */
public class ResponeProcessor {
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

                    Peer peer = new Peer();
                    MySQL a = new MySQL();
                    peer.setPeerAddress(a.examinePeerAddress());
                    peer.setPeerIp(a.examinePeerIp());
                    peer.setTime(a.examineTime());
                    String json = new Gson().toJson(peer);
                    ByteBuffer outBuffer = ByteBuffer.wrap(json.getBytes());

                    buffer.put(outBuffer);
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
