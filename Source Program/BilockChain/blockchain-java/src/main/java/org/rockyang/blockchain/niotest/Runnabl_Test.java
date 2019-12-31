/*
package org.rockyang.blockchain.niotest;


import org.bitcoinj.core.Transaction;
import org.rockyang.blockchain.P2P.ServerHandlerImpl;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Runnabl_Test implements Runnable {
    testst testst;
    public Runnabl_Test() {
    }
    public Runnabl_Test(testst name) {
       this.testst = name;
     }
    public void run() {
        try {
            Thread.sleep(2000);
            System.out.println("线程 执行:");
             String a ="sda";
             ByteBuffer a =new ByteBuffer();
            testst.settx(a);
           } catch (InterruptedException e) {
            e.printStackTrace();
            }
        }
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        testst S = new testst();
        Future<testst> future = executor.submit(new Runnabl_Test(S), S);
        Runnabl_Test a = new  Runnabl_Test();
       System.out.println("返回的结果 name: " + future.get().gettransaction());
        }
}*/
