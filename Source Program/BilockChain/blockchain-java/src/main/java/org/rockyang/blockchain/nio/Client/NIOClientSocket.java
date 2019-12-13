package org.rockyang.blockchain.nio.Client;

import com.google.gson.Gson;
import org.rockyang.blockchain.nio.Serializable.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *  NIO 客户端
 *  @author Wang HaiTian
 */
public class NIOClientSocket {

    public static void main(String[] args) throws IOException {
        //使用线程模拟用户 并发访问
        //for (int i = 0; i < 2; i++) {
            new Thread(){
                public void run() {
                    try {
                        //1.创建SocketChannel
                        SocketChannel socketChannel=SocketChannel.open();
                        //2.连接服务器
                        socketChannel.connect(new InetSocketAddress("192.168.0.11",8088));
                        //写数据
                        String msg="qianbao";
                        ByteBuffer buffer=ByteBuffer.allocate(1024);
                        buffer.put(msg.getBytes());
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
                        //Byte a =  buffer.get();
                        //String b = a.toString();
                        String b = bos.toString();
                        Peer peer = new Gson().fromJson(b, Peer.class);
                        System.out.println(peer.toString());
                        /*System.out.println("客户端收到:"+new String(bos.toByteArray()));*/
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }.start();

    }
}
