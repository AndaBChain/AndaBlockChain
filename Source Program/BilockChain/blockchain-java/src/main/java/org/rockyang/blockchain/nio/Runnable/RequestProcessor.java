package org.rockyang.blockchain.nio.Runnable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Service.*;
/**
 * 读操作的工具类
 * @author Wang HaiTian
 *
 */
public class RequestProcessor {
    //构造线程池
    private static ExecutorService  executorService  = Executors.newFixedThreadPool(10);
    public static void ProcessorRequest(final SelectionKey key){
        //获得线程并执行
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketChannel readChannel = (SocketChannel) key.channel();
                    // I/O读数据操作
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len = 0;
                    while (true) {
                        buffer.clear();
                        len = readChannel.read(buffer);
                        if (len == -1) break;
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            baos.write(buffer.get());
                        }
                    }
                    //System.out.println("服务器端接收到的数据："+ new String(baos.toByteArray()));
                    MySQL mysql = new MySQL();
                    Date date = new Date();
                    //当前时间
                    Timestamp nousedate = new Timestamp(date.getTime());
                    String a = new String(baos.toByteArray());
                    String ip =readChannel.socket().getInetAddress().toString();
                    String IpTest = readChannel.socket().getInetAddress().getHostName();
                    String str2 = ip.substring(1, ip.length());
                    System.out.println("测试mysql"+a+str2+nousedate);
                    List<String> list = mysql.examineAddress();
                    if (a.isEmpty()){
                        System.out.println("钱包地址为空");
                    }else if (list.contains(a)){
                        System.out.println("钱包地址已存在，钱包地址为："+a);
                        mysql.upold(a,nousedate);
                    }else  if (list.contains(a)!=true){
                        System.out.println("钱包地址暂不存在，钱包地址为："+a);
                        mysql.upnew(str2, a, nousedate);
                    }
                    //将数据添加到key中
                    key.attach(baos);
                    //将注册写操作添加到队列中
                    NIOServerSocket.addWriteQueen(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
