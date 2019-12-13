package org.rockyang.blockchain.niotest;

import org.bitcoinj.core.Transaction;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Future;

public class Server {

    private static int DEFAULT_PORT = 12345;
    private static ServerHandle serverHandle;
    public static void start(){
     start(DEFAULT_PORT);
    }

    public static synchronized void start(int port){
        if(serverHandle!=null)
            serverHandle.stop();
            serverHandle =new ServerHandle(port);
         new Thread(serverHandle,"Server").start();
    }
    public static void main(String[] args){
        server();
    }
    public static void server(){
        start();
    }
}



