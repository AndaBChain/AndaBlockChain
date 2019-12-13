package org.rockyang.blockchain.P2P;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;


public class NIOSocketServerTest {

    public static void main(String[] args) throws IOException {
        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 获取与此通道关联的服务器套接字
        ServerSocket socket = serverSocketChannel.socket();
        // 将 ServerSocket 绑定到特定地址（IP 地址和端口号）
        socket.bind(new InetSocketAddress("192.168.0.11", 8080));
        // 设置通道为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                // 将字节序列从此通道中读入给定的缓冲区
                socketChannel.read(byteBuffer);
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    List<String> list = new ArrayList<String>();
                    System.out.print((char) byteBuffer.get());
                }

                socketChannel.close();
            }


        }
    }
}