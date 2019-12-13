package org.rockyang.blockchain.P2P;

import org.apache.commons.lang3.SerializationUtils;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.net.base.ServerResponseVo;
import org.rockyang.blockchain.utils.SerializeUtils;
import org.springframework.http.server.ServerHttpResponse;

import java.util.concurrent.Callable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*import org.rockyang.blockchain.nio.nioClient;*/
import java.util.*;
import java.util.concurrent.Future;

/**
 *
 */
public class NioServer  {
    public Transaction transaction2 ;
    private int port;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(5);

    public static void main(String[] args){
         new NioServer(8080).start();
    }

    public NioServer(int port) {
        this.port = port;
    }

    public void init() {
        ServerSocketChannel ssc = null;
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("NioServer started ......");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }
    }

    public void accept(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
            System.out.println("accept a client : " + sc.socket().getInetAddress().getHostName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Future start() {
        this.init();
        while (true) {
            try {
                int events = selector.select();
                if (events > 0) {
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey key = selectionKeys.next();
                        selectionKeys.remove();
                        try {
                            if (key.isAcceptable()) {
                                accept(key);
                            } else {
                                Future f = service.submit( new NioServerHandler(key));
                               Object f1 = f.get();
                                System.out.println(f1);

                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class  NioServerHandler implements Runnable{

        private SelectionKey selectionKey;
        protected static final NetworkParameters params = MainNetParams.get();
        public  NioServerHandler(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
        }

        @Override
        public void run() {
            try {
                if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(2048);
                    socketChannel.read(buffer);
                    buffer.flip();
                   /* System.out.println("收到客户端"+socketChannel.socket().getInetAddress().getHostName()+"的数据："+new String(buffer.array()));*/
                    BitcoinSerializer deserialize2 = new BitcoinSerializer(params,true);
                    Transaction transaction = (Transaction) deserialize2.deserialize(buffer);
                    String tx1 =transaction.toString();
                    System.out.println("收到客户端"+tx1);
                    byte[] b =tx1.getBytes();
                    for (int i=0;i<=b.length;i++){
                        if (b[i]!=0){
                            byte l=b[i];
                            byte[] z=new byte[]{l};
                            String s=new String(z);
                            /*System.out.println(s);*/
                        }
                    }
                    //将数据添加到key中
                    ByteBuffer outBuffer = ByteBuffer.wrap(buffer.array());
                    socketChannel.write(outBuffer);// 将消息回送给客户端
                    selectionKey.cancel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
