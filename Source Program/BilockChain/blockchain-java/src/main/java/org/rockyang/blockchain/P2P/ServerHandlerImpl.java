package org.rockyang.blockchain.P2P;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.rockyang.blockchain.niotest.testst;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.*;

//nio 服务端
public class ServerHandlerImpl implements Runnable {
    public  testst testst;
    //1 多路复用器
    private Selector selector;
    //2 建立缓冲区
    private ByteBuffer readBuf=ByteBuffer.allocate(1024);
    private ByteBuffer writeBuf=ByteBuffer.allocate(1024);
    private Transaction transaction = null ;
    //构造函数
    public ServerHandlerImpl(int port){
        try {
            //1 打开多路复用器
            this.selector=Selector.open();
            //2 打开服务器通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //3 设置服务器通道为非阻塞方式
            ssc.configureBlocking(false);
            //4 绑定ip
            ssc.bind(new InetSocketAddress(8080));
            //5 把服务器通道注册到多路复用器上,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
            ssc.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器已经启动.....");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(transaction == null){//一直循环
            try {
                this.selector.select();//多路复用器开始监听
                //获取已经注册在多了复用器上的key通道集
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                //遍历
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();//获取key
                    //如果是有效的
                    if(key.isValid()){
                        // 如果为阻塞状态,一般是服务端通道
                        if(key.isAcceptable()){
                          this.accept(key);
                        }
                        // 如果为可读状态,一般是客户端通道
                        if(key.isReadable()){
                            this.read(key);

                            if (transaction != null) {
                                System.out.println(this.transaction.toString() + "hello2");
                            }
                        }
                    }
                    //从容器中移除处理过的key
                    if (transaction != null) {
                        System.out.println(this.transaction.toString() + "hello3");
                    }
                    keys.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (transaction != null) {
                System.out.println(this.transaction.toString() + "hello4");
            }
        }

    }
    //从客户端通道获取数据并进行处理
    private void read(SelectionKey key) {
        Transaction transaction2 = null ;
        try {
            if (key.isReadable()) {
                try {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(2048);
                socketChannel.read(buffer);
                buffer.flip();
                Thread.sleep(12000);
                testst.settx(buffer);
                /* System.out.println("收到客户端"+socketChannel.socket().getInetAddress().getHostName()+"的数据："+new String(buffer.array()));*/
                BitcoinSerializer deserialize2 = new BitcoinSerializer(MainNetParams.get(),true);
                transaction2 = (Transaction) deserialize2.deserialize(ByteBuffer.wrap(buffer.array()));
                String tx1 =transaction2.toString();
                System.out.println("收到客户端"+tx1);
                    /*Thread.sleep(10000);*/
                    System.out.println(transaction2.toString()+"hello");
                //将数据添加到key中
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //接受一个客户端socket进行处理
    private void accept(SelectionKey key) {
        try {
            //1 获取服务通道
            ServerSocketChannel ssc =  (ServerSocketChannel) key.channel();
            //2 执行阻塞方法,当有客户端请求时,返回客户端通信通道
            SocketChannel sc = ssc.accept();
            //3 设置阻塞模式
            sc.configureBlocking(false);
            //4 注册到多路复用器上，并设置可读标识
            sc.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Transaction main() throws Exception{
       ServerHandlerImpl S =new ServerHandlerImpl(8080);
        return S.mai();
    }
    public Transaction mai() {
        Transaction transaction = null;
        //启动服务器
        ExecutorService executor = Executors.newFixedThreadPool(2);
        testst S = new testst();
        Future<testst> future = executor.submit(new ServerHandlerImpl(8080), S);
        try {
            ByteBuffer buffer = future.get().gettx();
            BitcoinSerializer deserialize2 = new BitcoinSerializer(MainNetParams.get(),true);
            this.transaction = (Transaction) deserialize2.deserialize(ByteBuffer.wrap(buffer.array()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transaction;
    }


}