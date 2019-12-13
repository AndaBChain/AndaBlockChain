package org.rockyang.blockchain.niotest;

import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;

import java.nio.channels.SelectionKey;

import java.nio.channels.Selector;

import java.nio.channels.SocketChannel;

import java.util.Iterator;

import java.util.Set;

/**
 * NIO客户端
 */

public class ClientHandle implements Runnable{
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean started;

    public ClientHandle(String ip,int port) {
        this.host = ip;
        this.port = port;
        try{
            //创建选择器
            selector = Selector.open();
            //打开监听通道
            socketChannel = SocketChannel.open();
            //设置阻塞模式
            socketChannel.configureBlocking(false);//开启非阻塞模式
            started =true;
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
        try{
            System.out.println("asdasd");
            doConnect();
            String a = "hello";
            sendMsg(a);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //循环遍历selector
        while(started){
            try{
                //无论是否有读写事件发生，selector每隔1s被唤醒一次
                selector.select(1000);
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
            }catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //selector释放资源
        if(selector != null)
            try{
                selector.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            SocketChannel sc = (SocketChannel) key.channel();
            if(key.isConnectable()){
                if(sc.finishConnect());
                else System.exit(1);
            }
            //读消息
            if(key.isReadable()){
                //创建ByteBuffer，并开辟一个1M的缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                //读取请求码流，返回读取到的字节数
                int readBytes = sc.read(buffer);
                //读取到字节，对字节进行编解码
                if(readBytes>0){
                    //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                    buffer.flip();
                    //根据缓冲区可读字节数创建字节数组
                    byte[] bytes = new byte[buffer.remaining()];
                    //将缓冲区可读字节数组复制到新建的数组中
                    buffer.get(bytes);
                    String result =new String(bytes,"UTF-8");
                    System.out.println("客户端收到消息：" + result);
                }


                //关闭，释放资源
                else if(readBytes<0){
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    //异步发送消息
    private void doWrite(SocketChannel channel,String request) throws IOException{
        System.out.println("5");
        //将消息编码为字节数组
        byte[] bytes = request.getBytes();
        System.out.println("6");
        //根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        System.out.println("7");
        //将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        System.out.println("8");
        //flip操作
        writeBuffer.flip();
        //发送缓冲区的字节数组
        channel.write(writeBuffer);
    }

    private void doConnect() throws IOException{
        System.out.println("4");
        if(socketChannel.connect(new InetSocketAddress(host,port)));
        else socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void sendMsg(String msg) throws Exception{
        System.out.println("3");
        socketChannel.register(selector, SelectionKey.OP_READ);
        doWrite(socketChannel, msg);
    }
}