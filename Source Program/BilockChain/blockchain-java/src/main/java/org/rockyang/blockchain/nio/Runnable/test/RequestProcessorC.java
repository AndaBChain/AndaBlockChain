package org.rockyang.blockchain.nio.Runnable.test;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.UnitTestParams;

import org.rockyang.blockchain.nio.Service.NIOServerSocketC;
import org.rockyang.blockchain.testing.FakeTxBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 读操作的工具类
 * @author Wang HaiTian
 *
 */
public class RequestProcessorC {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = UnitTestParams.get();
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
                        BitcoinSerializer deserialize2 = new BitcoinSerializer(params,true);
                        Transaction transaction = (Transaction) deserialize2.deserialize(buffer);
                        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
                        String tx1 =transaction.toString();
                        transaction.getInput(0).connect(tx, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
                        transaction.verify();// 交易的基本数据校验
                        transaction.getInput(0).verify();// 交易的验签合法
                        System.out.println("收到客户端"+tx1);
                        transaction.verify();
                        baos.write("1".getBytes());

                    }

                    //System.out.println("服务器端接收到的数据："+ new String(baos.toByteArray()));
                    //将数据添加到key中
                    key.attach(baos);
                    //将注册写操作添加到队列中
                    NIOServerSocketC.addWriteQueen(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
