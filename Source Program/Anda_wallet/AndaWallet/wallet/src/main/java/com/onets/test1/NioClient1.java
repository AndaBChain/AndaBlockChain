package com.onets.test1;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;
import com.onets.wallet.Constants;
import com.onets.wallet.util.BitcoinSerializer;

import org.bitcoinj.core.Message;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 */
public class NioClient1 {
    private static final String TAG = "NioClient1";

    private static final int SIZE = 1024;
    private static NioClient1 instance = new NioClient1();

    private static final String host = "192.168.0.11";
    private static final int port = 8080;

    public String IP = "192.168.0.11";// 192.168.0.11
    public int CLIENT_PORT = 4444;// 4444
    private SocketChannel channel;
    private Selector selector = null;

    String encoding = System.getProperty("file.encoding");
    Charset charset = Charset.forName(encoding);

    public NioClient1(){

    }

    public static NioClient1 getInstance(){
        return instance;
    }

    public void send(String content) throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open();
        // channel = SocketChannel.open(new InetSocketAddress(IP,CLIENT_PORT));
        InetSocketAddress remote = new InetSocketAddress(IP, CLIENT_PORT);
        channel.connect(remote);
        // 设置该sc以非阻塞的方式工作
        channel.configureBlocking(false);
        // 将SocketChannel对象注册到指定的Selector
        // SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT
        channel.register(selector, SelectionKey.OP_READ);//这里注册的是read读，即从服务端读数据过来
        // 启动读取服务器数据端的线程
        new ClientThread().start();
        channel.write(charset.encode(content));
        // 创建键盘输入流
        Scanner scan = new Scanner(System.in);//这里向服务端发送数据，同时启动了一个键盘监听器
        while (scan.hasNextLine()) {
            Log.d(TAG, Constants.LOG_LABLE + "send: 输入数据");
            // 读取键盘的输入
            String line = scan.nextLine();
            // 将键盘的内容输出到SocketChanenel中
            channel.write(charset.encode(line));
        }
        scan.close();
    }

    /**
     * 从服务端读入数据的线程
     *
     */
    private class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                while (selector.select() > 0) {
                    // 遍历每个有可能的IO操作的Channel对银行的SelectionKey
                    for (SelectionKey sk : selector.selectedKeys()) {
                        // 删除正在处理的SelectionKey
                        selector.selectedKeys().remove(sk);
                        // 如果该SelectionKey对应的Channel中有可读的数据
                        if (sk.isReadable()) {
                            // 使用NIO读取Channel中的数据
                            SocketChannel sc = (SocketChannel) sk.channel();
                            String content = "";
                            ByteBuffer bff = ByteBuffer.allocate(SIZE);
                            while (sc.read(bff) > 0) {
                                sc.read(bff);
                                bff.flip();
                                content += charset.decode(bff);
                            }
                            // 打印读取的内容
                            Log.d(TAG, Constants.LOG_LABLE + "服务端返回数据:" + content);
                            // 处理下一次读
                            sk.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }

            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    /**
     * TCP 处理 线程
     */
    class TCPClientReadThread implements Runnable {
        private Selector selector;

        public TCPClientReadThread(Selector selector) {
            this.selector = selector;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                channel.configureBlocking(false);
                // selector.select(3000);
                channel.register(selector, SelectionKey.OP_READ);

                while (true) {
                    if (selector.select(1000) > 0) {
                        // 遍历每个有可用IO操作Channel对应的SelectionKey
                        for (SelectionKey sk : selector.selectedKeys()) {
                            // 如果该SelectionKey对应的Channel中有可读的数据
                            if (sk.isReadable()) {
                                // 使用NIO读取Channel中的数据
                                SocketChannel sc = (SocketChannel) sk.channel();
                                // 将字节转化为为UTF-8的字符串
                                receiveData(sc);
                                // 为下一次读取作准备
                                sk.interestOps(SelectionKey.OP_READ);
                            } else if (sk.isWritable()) {
                                // 取消对OP_WRITE事件的注册
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));
                                SocketChannel sc = (SocketChannel) sk.channel();

                                // 此步为阻塞操作，直到写入操作系统发送缓冲区或者网络IO出现异常
                                // 返回的为成功写入的字节数，若缓冲区已满，返回0
                                int writeenedSize = sc.write(buffer);

                                // 若未写入，继续注册感兴趣的OP_WRITE事件
                                if (writeenedSize == 0) {
                                    sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
                                }
                            } else if (sk.isConnectable()) {
                                SocketChannel sc = (SocketChannel) sk.channel();
                                sc.configureBlocking(false);

                                // 注册感兴趣的IO事件，通常不直接注册写事件，在发送缓冲区未满的情况下
                                // 一直是可写的，所以如果注册了写事件，而又不写数据，则很容易造成CPU消耗100%
                                // SelectionKey sKey = sc.register(selector,
                                // SelectionKey.OP_READ);

                                // 完成连接的建立
                                sc.finishConnect();
                            }
                            // 删除正在处理的SelectionKey
                            selector.selectedKeys().remove(sk);
                        }
                    }
                    if (selector.select(1000) <= 0) {
                        Thread.sleep(1000);
                        continue;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 客户端发送数据
     *
     * @param channel
     * @param bytes
     * @throws Exception
     */
    protected void sendData(SocketChannel channel, byte[] bytes) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        channel.write(buffer);
        //channel.socket().shutdownOutput();
    }

    protected void sendData(SocketChannel channel, String data) throws Exception {
        this.sendData(channel, data.getBytes());
    }

    /**
     * 接受服务端的数据
     *
     * @param channel
     * @return
     * @throws Exception
     */
    protected void receiveData(SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int count = 0;
        while ((count = channel.read(buffer)) != -1) {
            if (count == 0) {
                Thread.sleep(100); // 等等一下
                continue;
            }
            // 转到最开始
            buffer.flip();
            while (buffer.remaining() > 0) {
                Byte a =  buffer.get();
                String b = a.toString();
                Peer peer = new Gson().fromJson(b, Peer.class);
                System.out.println(peer.toString());
                System.out.print((char) buffer.get());
            }
            buffer.clear();
        }
    }

    public static void NIOClient(Transaction tx)  {
        Log.d(TAG, Constants.LOG_LABLE + "NIOSCTest: " + tx);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NioClient1 client = new NioClient1();
                client.connect(host, port);
                client.listen(tx);
            }
        }).start();
    }

    public void connect(String host, int port) {
        try {
            SocketChannel sc = SocketChannel.open();
            Log.d(TAG, Constants.LOG_LABLE + "connect: SCopen " + sc.isOpen());
            sc.configureBlocking(false);
            this.selector = Selector.open();
            Log.d(TAG, Constants.LOG_LABLE + "connect: Sopen " + this.selector.isOpen());
            sc.register(selector, SelectionKey.OP_CONNECT);
            sc.connect(new InetSocketAddress(host, port));
            Log.d(TAG, Constants.LOG_LABLE + "connect: " + sc.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(Transaction tx) {
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

                            /*作为一个中间储存类，在序列化中将其序列化后的二进制码写入其中*/
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            /*将tx赋予message，作为序列化的参数需要这种格式*/
                            Message txmessage = tx;
                            /*为BitcoinSerializer赋予params参数，序列化tx需要这种参数*/
                            BitcoinSerializer deSerialize = new BitcoinSerializer(MainNetParams.get(), true);
                            deSerialize.serialize(txmessage, out);
                            Log.d(TAG, Constants.LOG_LABLE + "listen: serialize");

                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            ByteBuffer buffer = ByteBuffer.allocate(4096);
                            /*通过调用out，即可取出储存其中的tx序列化后的二进制码*/
                            buffer.put(out.toByteArray());
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
    }

    public void NioClientSocket(String msg){

        try {
            //1.创建SocketChannel
            SocketChannel socketChannel = SocketChannel.open();
            //2.连接服务器
            socketChannel.connect(new InetSocketAddress("192.168.0.11",8989));

            //写数据
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
            Log.d(TAG, Constants.LOG_LABLE + "NioClientSocket: " + peer.toString());

            Log.d(TAG, Constants.LOG_LABLE + "NioClientSocket: 客户端收到:" + new String(bos.toByteArray()));

            socketChannel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
