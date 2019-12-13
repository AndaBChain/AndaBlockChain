package org.rockyang.blockchain.niotest;

import org.rockyang.blockchain.db.MySql.MySQL;

import javax.validation.constraints.Max;
import java.util.List;

public class Client {

    private static String DEFAULT_HOST = "127.0.0.1";

    private static int DEFAULT_PORT = 8088;

    private static ClientHandle clientHandle;

    public static void start()  {
        MySQL a = new MySQL();
        List<String> alisr = a.examine();
        for (int b=1;b<=5;b++) {
            String DEFAULT_HOST = alisr.get(b);
            start(DEFAULT_HOST, DEFAULT_PORT);
        }
    }

    public static synchronized void start(String ip,int port){
        if(clientHandle!=null)
            clientHandle.stop();
            clientHandle =new ClientHandle(ip,port);
        new Thread(clientHandle,"Server").start();

    }

//向服务器发送消息

    public static boolean sendMsg(String msg) throws Exception{
        System.out.println("2");
        if(msg.equals("q")) return false;
        clientHandle.sendMsg(msg);
        return true;
    }

    public static void main(String[] args)  {
        start();
    }

}