/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.core;

import com.google.common.util.concurrent.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.rockyang.blockchain.testing.*;
import org.bitcoinj.utils.*;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.*;
import static org.bitcoinj.core.Coin.*;
import static org.junit.Assert.*;
//广播事物
@RunWith(value = Parameterized.class)
public class TransactionBroadcastTest extends TestWithPeerGroup {

    @Parameterized.Parameters
    public static Collection<ClientType[]> parameters() {
        return Arrays.asList(new ClientType[] {ClientType.NIO_CLIENT_MANAGER},
                new ClientType[] {ClientType.BLOCKING_CLIENT_MANAGER});
    }
    //事务广播测试
    public TransactionBroadcastTest(ClientType clientType) {
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
    @Test
    public void fourPeers() throws Exception {
        InboundMessageQueuer[] channels = { connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
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
    //后期进展回调
    @Test
    public void lateProgressCallback() throws Exception {
        // Check that if we register a progress callback on a broadcast after the broadcast has started, it's invoked
        // immediately with the latest state. This avoids API users writing accidentally racy code when they use
        // a convenience method like peerGroup.
        // broadcastTransaction
        // .//检查如果我们在广播启动后在广播上注册一个进度回调函数，它就会被调用
        ////立即显示最新的状态。这避免了API用户在使用时意外编写生动的代码
        ////像peerGroup.broadcastTransaction这样的方便方法。

        InboundMessageQueuer[] channels = { connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST, CENT, address);
        tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
        TransactionBroadcast broadcast = peerGroup.broadcastTransaction(tx);
        inbound(channels[1], InventoryMessage.with(tx));
        pingAndWait(channels[1]);
        final AtomicDouble p = new AtomicDouble();
        broadcast.setProgressCallback(new TransactionBroadcast.ProgressCallback() {
            @Override
            public void onBroadcastProgress(double progress) {
                p.set(progress);
            }
        }, Threading.SAME_THREAD);
        assertEquals(1.0, p.get(), 0.01);
    }

    //拒绝处理
    // 涉及连接
    // 。
    @Test
    public void rejectHandling() throws Exception {
        InboundMessageQueuer[] channels = { connectPeer(0), connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
        TransactionBroadcast broadcast = new TransactionBroadcast(peerGroup, tx);
        ListenableFuture<Transaction> future = broadcast.broadcast();
        // 0 and 3 are randomly selected to receive the broadcast
        // 随机选择0和3接收广播。.
        assertEquals(tx, outbound(channels[1]));
        assertEquals(tx, outbound(channels[2]));
        assertEquals(tx, outbound(channels[4]));
        RejectMessage reject = new RejectMessage(UNITTEST, RejectMessage.RejectCode.DUST, tx.getTxId(), "tx", "dust");
        inbound(channels[1], reject);
        inbound(channels[4], reject);
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(RejectedTransactionException.class, e.getCause().getClass());
        }
    }
    //重试失败的广播
    @Test
    public void retryFailedBroadcast() throws Exception {
        // If we create a spend, it's sent to a peer that swallows it, and the peergroup is removed/re-added then
        // the tx should be broadcast again
        // //如果我们创建一个花费，它被发送给一个同伴，这个同伴会吞下它，然后peergroup被删除/重新添加
        //// tx应该再播一次。
        InboundMessageQueuer p1 = connectPeer(1);
        connectPeer(2);

        // Send ourselves a bit of money
        // 给我们自己寄点钱。.
        Block b1 = FakeTxBuilder.makeSolvedTestBlock(blockStore, address);
        inbound(p1, b1);
        assertNull(outbound(p1));
        assertEquals(FIFTY_COINS, wallet.getBalance());

        // Now create a spend, and expect the announcement on p1
        // 现在创建一个花费，并期待p1上的公告。.
        Address dest = LegacyAddress.fromKey(UNITTEST, new ECKey());
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, dest, COIN);
        assertFalse(sendResult.broadcastComplete.isDone());
        Transaction t1;
        {
            Message m;
            while (!((m = outbound(p1)) instanceof Transaction));
            t1 = (Transaction) m;
        }
        assertFalse(sendResult.broadcastComplete.isDone());

        // p1 eats it :( A bit later the PeerGroup is taken down
        // p1吃了它:(过了一会儿，PeerGroup被取消了。.
        peerGroup.removeWallet(wallet);
        peerGroup.addWallet(wallet);

        // We want to hear about it again. Now, because we've disabled the randomness for the unit tests it will
        // re-appear on p1 again. Of course in the real world it would end up with a different set of peers and
        // select randomly so we get a second chance
        // //我们想再听一遍。现在，因为我们已经禁用了单元测试的随机性
        ////再次出现在p1上。当然，在现实世界中，它最终会有一群不同的同伴
        ////随机选择，这样我们就有了第二次机会。.
        Transaction t2 = (Transaction) outbound(p1);
        assertEquals(t1, t2);
    }
    //对等组钱包集成
    @Test
    public void peerGroupWalletIntegration() throws Exception {
        // Make sure we can create spends, and that they are announced. Then do the same with offline mode.
        //确保我们能够创造开支，并将其公布。然后在脱机模式下执行相同的操作。
        // Set up connections and block chain.
        //建立连接和区块链
        VersionMessage ver = new VersionMessage(UNITTEST, 2);
        ver.localServices = VersionMessage.NODE_NETWORK;
        InboundMessageQueuer p1 = connectPeer(1, ver);
        InboundMessageQueuer p2 = connectPeer(3);

        // Send ourselves a bit of money.
        //给我们自己发送比特币
        Block b1 = FakeTxBuilder.makeSolvedTestBlock(blockStore, address);
        inbound(p1, b1);
        pingAndWait(p1);
        assertNull(outbound(p1));
        assertEquals(FIFTY_COINS, wallet.getBalance());

        // Check that the wallet informs us of changes in confidence as the transaction ripples across the network
        // 检查钱包是否在网络上交易波动时通知我们信心的变化。.
        final Transaction[] transactions = new Transaction[1];
        wallet.addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                transactions[0] = tx;
            }
        });

        // Now create a spend, and expect the announcement on p1
        // 现在创建一个花费，并期待p1上的公告。.
        Address dest = LegacyAddress.fromKey(UNITTEST, new ECKey());
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, dest, COIN);
        assertNotNull(sendResult.tx);
        Threading.waitForUserCode();
        assertFalse(sendResult.broadcastComplete.isDone());
        assertEquals(transactions[0], sendResult.tx);
        assertEquals(0, transactions[0].getConfidence().numBroadcastPeers());
        transactions[0] = null;
        Transaction t1;
        {
            peerGroup.waitForJobQueue();
            Message m = outbound(p1);
            // Hack: bloom filters are recalculated asynchronously to sending transactions to avoid lock
            // inversion, so we might or might not get the filter/mempool message first or second
            // 破解:布鲁姆过滤器是重新计算异步发送事务，以避免锁定
            ////反转，所以我们可能会先得到过滤器/mempool消息，也可能不会先得到。.
            while (!(m instanceof Transaction)) m = outbound(p1);
            t1 = (Transaction) m;
        }
        assertNotNull(t1);
        // 49 BTC in change
        // 49换BTC。.
        assertEquals(valueOf(49, 0), t1.getValueSentToMe(wallet));
        // The future won't complete until it's heard back from the network on p2
        // 直到p2上的网络返回消息，未来才会完成。.
        //广播:等待广播所需的2个对等点，我们有2个…
        //运行:broadcastTransaction:我们有两个对等点，将d30b3f2d8c2cf34c19423e744b1e1c2bb8c4602f6456bd7c7d1ec7bb286f65添加到内存池
        //运行:发送给1个对等点，将等待1，
        // 发送给:Peer{[127.0.0.1]:2001, version=70012, subVer=/bitcoinj:0.16-SNAPSHOT/， services=1 (NETWORK)，
        // time=2019-07-23 20:58:03, height=2}
        InventoryMessage inv = new InventoryMessage(UNITTEST);
        inv.addTransaction(t1);
        inbound(p2, inv);
        pingAndWait(p2);
        Threading.waitForUserCode();
        assertTrue(sendResult.broadcastComplete.isDone());
        assertEquals(transactions[0], sendResult.tx);
        assertEquals(1, transactions[0].getConfidence().numBroadcastPeers());
        // Confirm it
        // 确认.
        Block b2 = FakeTxBuilder.createFakeBlock(blockStore, Block.BLOCK_HEIGHT_GENESIS, t1).block;
        inbound(p1, b2);
        pingAndWait(p1);
        assertNull(outbound(p1));

        // Do the same thing with an offline transaction
        // 对离线事务执行相同的操作。.
        peerGroup.removeWallet(wallet);
        SendRequest req = SendRequest.to(dest, valueOf(2, 0));
        Transaction t3 = checkNotNull(wallet.sendCoinsOffline(req));
        assertNull(outbound(p1));  // Nothing sent 发送失败.
        // Add the wallet to the peer group (simulate initialization). Transactions should be announced
        // 将钱包添加到对等组(模拟初始化)。交易应该公布。.
        peerGroup.addWallet(wallet);
        // Transaction announced to the first peer. No extra Bloom filter because no change address was needed
        // 向第一个对等点声明事务。没有额外的Bloom过滤器，因为不需要更改地址。.
        assertEquals(t3.getTxId(), outbound(p1).getHash());
    }
}
