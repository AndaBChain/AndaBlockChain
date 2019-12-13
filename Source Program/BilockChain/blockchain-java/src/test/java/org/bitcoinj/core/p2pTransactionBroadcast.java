package org.bitcoinj.core;


import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.SerializationUtils;
import org.bitcoinj.utils.Threading;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.rockyang.blockchain.P2P.ServerHandlerImpl;
import org.rockyang.blockchain.net.base.ServerResponseVo;
import org.rockyang.blockchain.niotest.Server;
import org.rockyang.blockchain.testing.FakeTxBuilder;
import org.rockyang.blockchain.testing.InboundMessageQueuer;
import org.rockyang.blockchain.testing.TestWithNetworkConnections;
import org.rockyang.blockchain.testing.TestWithPeerGroup;
import org.rockyang.blockchain.utils.SerializeUtils;
import org.springframework.http.server.ServerHttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bitcoinj.core.TransactionBroadcastTest;

import static org.bitcoinj.core.Coin.CENT;
import static org.junit.Assert.*;

//广播事物
public class p2pTransactionBroadcast extends TestWithPeerGroup {


    @Parameterized.Parameters
    public static Collection<ClientType[]> parameters() {
        return Arrays.asList(new ClientType[]{ClientType.NIO_CLIENT_MANAGER},
                new ClientType[]{ClientType.BLOCKING_CLIENT_MANAGER});
    }

    //事务广播测试
    public p2pTransactionBroadcast(ClientType clientType) {
        super(clientType);
    }

    //设置

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

    //四个同伴

    public void fourPeers() throws Exception {
        InboundMessageQueuer[] channels = {connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4)};
        ServerHandlerImpl SH = new ServerHandlerImpl(8080);
        org.bitcoinj.core.Transaction tx = SH.main();
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
        ListenableFuture<org.bitcoinj.core.Transaction> future = broadcast.broadcast();
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

}