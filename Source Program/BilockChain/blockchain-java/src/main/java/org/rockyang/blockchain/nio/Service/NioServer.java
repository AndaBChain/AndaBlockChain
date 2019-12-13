package org.rockyang.blockchain.nio.Service;

import com.google.gson.Gson;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Serializable.Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*import org.rockyang.blockchain.nio.nioClient;
* @author Wang HaiTian
* */

public class NioServer {
    public Transaction transaction2 ;
    public String IP = "192.168.0.11";
    public static  int port = 4444;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(5);

    public static void main(String[] args){
         new NioServer(4444).start();
    }

    public NioServer(int port) {
        this.port = port;
    }

    public void init() {
        ServerSocketChannel ssc = null;
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(IP, port));
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
                                service.submit( new NioServerHandler(key));
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
        // 对于以字符方式读取和处理的数据必须要进行字符集编码和解码
        String encoding = System.getProperty("file.encoding");
        // 加载字节编码集
        Charset charse = Charset.forName(encoding);
        @Override
        public void run() {
            try {
                if (selectionKey.isReadable()) {
                    String content = "";
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(2048);
                    //buffer.flip();
                    socketChannel.read(buffer);
                    CharBuffer cb = charse.decode((ByteBuffer) buffer.flip());
                    content = cb.toString();
                    buffer.clear();


                    System.out.println("收到客户端"+socketChannel.socket().getInetAddress().getHostName()+"的数据："+new String(buffer.array()));
                    //将数据添加到key中serverChannel1.accept()
                    String ip =socketChannel.socket().getInetAddress().toString();
                    String str2 = ip.substring(1, ip.length());
                    /*byte[] content = buffer.array();
                    String bf = new String(content,"UTF-8");
                    System.out.println(bf+"这是测试消息");*/
                    MySQL mysql = new MySQL();
                    Date date = new Date();
                    //当前时间
                    Timestamp nousedate = new Timestamp(date.getTime());
                    List <String> list = mysql.examineAddress();
                    int AddressRepeat= 0;
                    String newaddress = content;
                    if(newaddress==""){
                        AddressRepeat=-1;
                        System.out.println("消息为空");
                    }
                    if(newaddress==null){
                        AddressRepeat=-1;
                        System.out.println("消息为空");
                    }
                    for(int s1=0;s1<= list.size();s1++){
                        if(list.get(s1).equals(newaddress)){
                            AddressRepeat = 1;
                        }
                     }
                    System.out.println(AddressRepeat);
                    if (AddressRepeat==0) {
                        System.out.println("新的钱包");
                       mysql.upnew(str2, newaddress, nousedate);
                    }else if (AddressRepeat == 1){
                       System.out.println("已有的钱包");
                       mysql.upold(str2,nousedate);
                    }
                    System.out.println(newaddress+"这是测试消息2");
                    Peer peer = new Peer();
                    MySQL a = new MySQL();
                    List<Object> TIME = a.examineTime();
                    List<Object> PeerIp = a.examinePeerIp();
                    List<Object> PeerAddress = a.examinePeerAddress();
                    for (int b=0;b<=TIME.size()-1;b++ ) {
                        List<Object> PeerAddressList = new ArrayList<Object>();
                        List<Object> PeerIpList = new ArrayList<Object>();
                        List<Object> TimeList = new ArrayList<Object>();
                        PeerAddressList.add(PeerAddress.get(b));
                        PeerIpList.add(PeerIp.get(b));
                        TimeList.add(TIME.get(b));
                        if (b==TIME.size()-1){
                            peer.setPeerAddress(PeerAddress);
                            peer.setPeerIp(PeerIp);
                            peer.setTime(TIME);
                        }
                    }
                    String json = new Gson().toJson(peer);
                    ByteBuffer outBuffer = ByteBuffer.wrap(json.getBytes());
                    socketChannel.write(outBuffer);// 将消息回送给客户端
                    selectionKey.cancel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
