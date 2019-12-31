package org.bitcoinj.core;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;
import org.bitcoinj.utils.Threading;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.rockyang.blockchain.net.base.ServerResponseVo;
import org.rockyang.blockchain.testing.FakeTxBuilder;
import org.rockyang.blockchain.testing.InboundMessageQueuer;
import org.rockyang.blockchain.testing.TestWithPeerGroup;
import org.rockyang.blockchain.utils.SerializeUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import static org.junit.Assert.*;
@RunWith(value = Parameterized.class)
public class test extends TestWithPeerGroup {
    public String IP = "127.0.0.1";// 10.50.200.120
    public static final int PORT = 4444;
    private static final int SIZE = 256;
    // 对于以字符方式读取和处理的数据必须要进行字符集编码和解码
    String encoding = System.getProperty("file.encoding");
    // 加载字节编码集
    Charset charse = Charset.forName(encoding);
    @Parameterized.Parameters
    public static Collection<ClientType[]> parameters() {
        return Arrays.asList(new ClientType[] {ClientType.NIO_CLIENT_MANAGER},
                new ClientType[] {ClientType.BLOCKING_CLIENT_MANAGER});
    }
    //事务广播测试
    public test(ClientType clientType) {
        super(clientType);
    }
    @Override
    @Before
    public void setUp() throws Exception {
        Utils.setMockClock(); // Use mock clock使用模拟时钟
        super.setUp();
        // Fix the random permutation that TransactionBroadcast uses to shuffle the peers
        // 修正事务广播用于洗牌对等节点的随机排列。.
        TransactionBroadcast.random = new Random(0);
        peerGroup.setMinBroadcastConnections(2);
        peerGroup.start();
    }
    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }
    public void fourPeers(Transaction tx) throws Exception {
        InboundMessageQueuer[] channels = { connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
        TransactionBroadcast broadcast = new TransactionBroadcast(peerGroup, tx);
        final AtomicDouble lastProgress = new AtomicDouble();
        //广播交易
        broadcast.setProgressCallback(new TransactionBroadcast.ProgressCallback() {
            @Override
            public void onBroadcastProgress(double progress) {
                lastProgress.set(progress);
            }
        });
        ListenableFuture<Transaction> future = broadcast.broadcast();
        assertFalse(future.isDone());
        assertEquals(0.0, lastProgress.get(), 0.0);
        // We expect two peers to receive a tx message, and at least one of the others must announce for the future to
        // complete successfully
        // 我们期望两个对等点接收tx消息，并且其他对等点中至少有一个必须为将来的成功完成而声明

        Message[] messages = {
                outbound(channels[0]),
                outbound(channels[1]),
                outbound(channels[2]),
                outbound(channels[3])
        };
        // 0 and 3 are randomly selected to receive the broadcast
        // 随机选择0和3接收广播。
        assertEquals(tx, messages[0]);
        assertEquals(tx, messages[3]);
        assertNull(messages[1]);
        assertNull(messages[2]);
        Threading.waitForUserCode();
        assertFalse(future.isDone());
        assertEquals(0.0, lastProgress.get(), 0.0);
        inbound(channels[1], InventoryMessage.with(tx));
        future.get();
        Threading.waitForUserCode();
        assertEquals(1.0, lastProgress.get(), 0.0);
        // There is no response from the Peer as it has nothing to do
        // 没有来自同行的响应，因为它无关紧要。.
        assertNull(outbound(channels[1]));
    }

    public void nioService() throws IOException {
        // NIO的通道channel中内容读取到字节缓冲区ByteBuffer时是字节方式存储的，
        // 分配两个字节大小的字节缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        SocketChannel ch = null;
        Selector selector = null;
        ServerSocketChannel serverChannel = null;

        try {
            // 打开通道选择器
            selector = Selector.open();
            // 打开服务端的套接字通道
            serverChannel = ServerSocketChannel.open();
            // 将服务端套接字通道连接方式调整为非阻塞模式
            serverChannel.configureBlocking(false);
            // serverChannel.socket().setReuseAddress(true);
            // 将服务端套接字通道绑定到本机服务端端口
            serverChannel.socket().bind(new InetSocketAddress(IP, PORT));
            // 将服务端套接字通道OP_ACCEP事件注册到通道选择器上
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server on port:" + PORT);
            while (true) {
                // 通道选择器开始轮询通道事件
                selector.select();
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    // 获取通道选择器事件键
                    SelectionKey skey = (SelectionKey) it.next();
                    it.remove();
                    // 服务端套接字通道发送客户端连接事件，客户端套接字通道尚未连接
                    if (skey.isAcceptable()) {
                        // 获取服务端套接字通道上连接的客户端套接字通道
                        ch = serverChannel.accept();
                        System.out.println("Accepted connection from:" + ch.socket());
                        // 将客户端套接字通过连接模式调整为非阻塞模式
                        ch.configureBlocking(false);
                        // 将客户端套接字通道OP_READ事件注册到通道选择器上
                        ch.register(selector, SelectionKey.OP_READ);
                    }
                    // 如果sk对应的Channel有数据需要读取
                    if (skey.isReadable()) {
                        // 获取该SelectionKey对银行的Channel，该Channel中有刻度的数据
                        SocketChannel sc = (SocketChannel) skey.channel();
                        String content = "";
                        // 开始读取数据
                        try {
                            content = receiverFromClient(sc,buffer);
                            // 将sk对应的Channel设置成准备下一次读取
                            skey.interestOps(SelectionKey.OP_READ);
                        } catch (IOException e) {// 如果捕获到该sk对银行的Channel出现了异常，表明
                            // Channel对应的Client出现了问题，所以从Selector中取消
                            // 从Selector中删除指定的SelectionKey
                            skey.cancel();
                            if (skey.channel() != null) {
                                skey.channel().close();
                            }
                        }
                        if (content.length() > 0) {
                            System.out.println(content);
                            String a =new String(content);
                            byte[] b =a.getBytes();
                            ServerResponseVo responseVo = (ServerResponseVo) SerializeUtils.unSerialize(b);
                            Transaction tx =(Transaction)responseVo.getItem();
                            try {
                                fourPeers(tx);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // 处理信息返回给客户端
                            sendToClient(selector,content);
                        }
                        //ch.write((ByteBuffer)buffer.rewind());
                        //buffer.clear();
                    }
                    if(skey.isWritable()){

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ch != null)
                ch.close();
            serverChannel.close();
            selector.close();
        }
    }

    /**
     * 向客户端发送数据
     * @param selector
     * @param content
     * @throws IOException
     */
    public void sendToClient(Selector selector,String content) throws IOException{
        // 遍历selector里注册的所有SelectionKey
        for (SelectionKey key1 : selector.keys()) {
            // 获取该key对应的Channel
            Channel targerChannel = key1.channel();
            // 如果该Channel是SocketChannel对象
            if (targerChannel instanceof SocketChannel) {
                // 将读取到的内容写入该Channel中
                SocketChannel dest = (SocketChannel) targerChannel;
                sendToClient(dest,content);
            }
        }
    }

    /**
     * 向指定频道发送数据
     * @param channel
     * @param data
     * @throws IOException
     */
    public void sendToClient(SocketChannel channel, String data) throws IOException {
        channel.write(charse.encode(data));
        //channel.socket().shutdownOutput();
    }

    /**
     * 接受来自客户端数据
     * @param channel
     * @param buffer
     * @return
     * @throws Exception
     */
    private String receiverFromClient(SocketChannel channel,ByteBuffer buffer) throws Exception {
        String content = "";
        //* 取客户端发送的数据
        // 开始读取数据
        channel.read(buffer);
        CharBuffer cb = charse.decode((ByteBuffer) buffer.flip());
        content = cb.toString();
        buffer.clear();
        return content;
    }

    /*连接网络通道*/
    public static void main(String[] args) {
        try {
            new nioService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
