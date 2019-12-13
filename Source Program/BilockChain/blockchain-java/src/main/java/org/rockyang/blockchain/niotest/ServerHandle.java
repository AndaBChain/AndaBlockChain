package org.rockyang.blockchain.niotest;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import java.util.concurrent.Future;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO服务端
 */

public class ServerHandle implements Runnable{
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean started;
    protected static final NetworkParameters params = UnitTestParams.get();
    private Transaction transaction;
    private static ServerHandle serverHandle;

    public static void main(String[] args) {
        if(serverHandle!=null)
            serverHandle.stop();
        serverHandle =new ServerHandle(12345);
        new Thread(serverHandle,"Server").start();
    }
    /**
     * 构造方法
     * @param port 指定要监听的端口号
     */
    public  ServerHandle(int port) {
        try{
            //创建选择器
            selector = Selector.open();
            //打开监听通道
            serverChannel = ServerSocketChannel.open();
            //如果为 true，则此通道 n将被置于阻塞模式；如果为 false，则此通道将被置于非阻塞模式
            serverChannel.configureBlocking(false);//开启非阻塞模式
            //绑定端口 backlog设为1024
             String IP = "127.0.0.1";
            serverChannel.socket().bind(new InetSocketAddress(IP, port),1024);
            //监听客户端连接请求
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            //标记服务器已开启
            started =true;
            System.out.println("服务器已启动，端口号：" + port);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(){
        started =false;
    }

    @Override
    public void run() {

        //循环遍历selector
        while(started){
            try{
                //无论是否有读写事件发生，selector每隔1s被唤醒一次
                selector.select(1000);
                //阻塞,只有当至少一个注册的事件发生的时候才会继续.
                //              selector.select();
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();
                SelectionKey key =null;
                while(it.hasNext()){
                    key = (SelectionKey) it.next();
                    it.remove();
                    try{
                        handleInput(key);
                    }catch(Exception e){
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }

            }catch(Throwable t){
                t.printStackTrace();
            }
        }

        //selector关闭后会自动释放里面管理的资源

        if(selector != null)
            try{
                selector.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            //处理新接入的请求消息
            if(key.isAcceptable()){
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            //通过ServerSocketChannel的accept创建SocketChannel实例
            //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
                SocketChannel sc = ssc.accept();
            //设置为非阻塞的
                sc.configureBlocking(false);
            //注册为读
                sc.register(selector, SelectionKey.OP_READ);
            }
            //读消息
            if(key.isReadable()){
                SocketChannel sc = (SocketChannel) key.channel();
                //创建ByteBuffer，并开辟一个1M的缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(2048);
                //读取请求码流，返回读取到的字节数
                sc.read(buffer);
                //读取到字节，对字节进行编解码
                    //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                    buffer.flip();
                    //根据缓冲区可读字节数创建字节数组
                    byte[] byt = buffer.array();
                    /*String a = new String(byt);*/
                    //将缓冲区可读字节数组复制到新建的数组中
                    BitcoinSerializer deserialize2 = new BitcoinSerializer(params,true);
                    this.transaction = (Transaction) deserialize2.deserialize(ByteBuffer.wrap(buffer.array()));
                    String tx1 =transaction.toString();
                    System.out.println(tx1);
                    /*buffer.get(bytes)；
                    String expression =new String(bytes,"UTF-8");
                    System.out.println("服务器收到消息：" + expression);*/
                    //发送应答消息
                    doWrite(sc,tx1);
                }
                //链路已经关闭，释放资源
                    key.cancel();
        }
    }

    //异步发送应答消息
    private void doWrite(SocketChannel channel,String response) throws IOException{
        //将消息编码为字节数组
        byte[] bytes = response.getBytes();
        //根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        //将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        //flip操作
        writeBuffer.flip();
        //发送缓冲区的字节数组
        channel.write(writeBuffer);
    }
}