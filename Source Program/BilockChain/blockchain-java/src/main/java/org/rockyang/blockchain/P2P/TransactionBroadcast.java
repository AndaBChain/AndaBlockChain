/*
 * Copyright 2013 Google Inc.
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

package org.rockyang.blockchain.P2P;

import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.util.concurrent.*;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.utils.*;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.*;

import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkState;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;

/**
 * Represents a single transaction broadcast that we are performing. A broadcast occurs after a new transaction is created
 * (typically by a {@link Wallet} and needs to be sent to the network. A broadcast can succeed or fail. A success is
 * defined as seeing the transaction be announced by peers via inv messages, thus indicating their acceptance. A failure
 * is defined as not reaching acceptance within a timeout period, or getting an explicit reject message from a peer
 * indicating that the transaction was not acceptable.
 * / * *
 * 表示我们正在执行的单个事务广播。广播发生在创建新事务之后
 * *(通常由一个{@link Wallet}发送到网络。广播可以成功也可以失败。一个成功
 * *定义为看到同行通过inv消息宣布交易，从而表示接受。一个失败
 * *定义为在超时期间未达到接受，或从对等方获得显式拒绝消息
 * *表示该交易不可接受。
 * * /
 */
public class TransactionBroadcast {
    private static final Logger log = LoggerFactory.getLogger(TransactionBroadcast.class);

    private final SettableFuture<org.bitcoinj.core.Transaction> future = SettableFuture.create();
    private final PeerGroup peerGroup;
    private final Transaction tx;
    private int minConnections;
    private int numWaitingFor;

    /** Used for shuffling the peers before broadcast: unit tests can replace this to make themselves deterministic.
     * 用于在广播之前打乱对等点:单元测试可以替换它，使它们具有确定性。
     * */
    @VisibleForTesting
    public static Random random = new Random();
    
    // Tracks which nodes sent us a reject message about this broadcast, if any. Useful for debugging.
    // 跟踪哪些节点向我们发送了关于此广播的拒绝消息(如果有的话)。用于调试。
    private Map<Peer, RejectMessage> rejects = Collections.synchronizedMap(new HashMap<Peer, RejectMessage>());

    /*比特币核心中的事务广播方法
    TransactionBroadcast2(PeerGroup peerGroup, Transaction tx) {
        创建一个可设定的未来
        this.future = SettableFuture.create();
        同步集合 拒绝 Map
        this.rejects = Collections.synchronizedMap(new HashMap());
        拒绝侦听器无名的类
        this.rejectionListener = new NamelessClass_1();
        对等节点
        this.peerGroup = peerGroup;
        事物
        this.tx = tx;
        最小连接
        this.minConnections = Math.max(1, peerGroup.getMinBroadcastConnections());
    }*/
    TransactionBroadcast(PeerGroup peerGroup, org.bitcoinj.core.Transaction tx) {
        this.peerGroup = peerGroup;
        this.tx = tx;
        this.minConnections = Math.max(1, peerGroup.getMinBroadcastConnections());
    }

    // 只用于模拟广播。broadcasts  方法名为事物广播
    private TransactionBroadcast(org.bitcoinj.core.Transaction tx) {
        this.peerGroup = null;
        this.tx = tx;
    }
   //创建模拟广播
    @VisibleForTesting
    public static TransactionBroadcast createMockBroadcast(org.bitcoinj.core.Transaction tx, final SettableFuture<org.bitcoinj.core.Transaction> future) {
        return new TransactionBroadcast(tx) {
            @Override
            public ListenableFuture<org.bitcoinj.core.Transaction> broadcast() {
                return future;
            }

            @Override
            public ListenableFuture<org.bitcoinj.core.Transaction> future() {
                return future;
            }
        };
    }

    public ListenableFuture<org.bitcoinj.core.Transaction> future() {
        return future;
    }
    //设置最小连接
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }
    //消息接收事件监听器
    private PreMessageReceivedEventListener rejectionListener = new PreMessageReceivedEventListener() {
        @Override
        public org.bitcoinj.core.Message onPreMessageReceived(Peer peer, Message m) {
            if (m instanceof RejectMessage) {
                RejectMessage rejectMessage = (RejectMessage)m;
                if (tx.getTxId().equals(rejectMessage.getRejectedObjectHash())) {
                    rejects.put(peer, rejectMessage);
                    int size = rejects.size();
                    long threshold = Math.round(numWaitingFor / 2.0);
                    if (size > threshold) {
                        log.warn("Threshold for considering broadcast rejected has been reached ({}/{})", size, threshold);
                        future.setException(new RejectedTransactionException(tx, rejectMessage));
                        peerGroup.removePreMessageReceivedEventListener(this);
                    }
                }
            }
            return m;
        }
    };
    //广播方法
    public ListenableFuture<Transaction> broadcast() {
        peerGroup.addPreMessageReceivedEventListener(Threading.SAME_THREAD, rejectionListener);
        log.info("Waiting for {} peers required for broadcast, we have {} ...", minConnections, peerGroup.getConnectedPeers().size());
        peerGroup.waitForPeers(minConnections).addListener(new EnoughAvailablePeers(), Threading.SAME_THREAD);
        return future;
    }
    //对等节点
    private class EnoughAvailablePeers implements Runnable {
        private Context context;

        public EnoughAvailablePeers() {
            this.context = Context.get();
        }

        @Override
        public void run() {
            Context.propagate(context);
            // We now have enough connected peers to send the transaction.
            // This can be called immediately if we already have enough. Otherwise it'll be called from a peer
            // thread
            // //我们现在有足够的连接对等点来发送事务。
            ////如果我们已经有足够的钱，这个可以马上调用。否则它将被一个对等点调用
            /// /线程。.

            // We will send the tx simultaneously to half the connected peers and wait to hear back from at least half
            // of the other half, i.e., with 4 peers connected we will send the tx to 2 randomly chosen peers, and then
            // wait for it to show up on one of the other two. This will be taken as sign of network acceptance. As can
            // be seen, 4 peers is probably too little - it doesn't taken many broken peers for tx propagation to have
            // a big effect
            // //我们将同时发送tx给连接的一半的对等点，并等待至少一半的对等点的回复
            ////另一半，即，连接4个节点后，我们将把tx发送给随机选择的2个节点，然后
            ////等它出现在另外两个中的一个上。这将被视为网络接受的标志。是可以
            ////可以看到，4个对等点可能太少了——tx传播并不需要很多坏的对等点
            ////影响很大。.
            List<Peer> peers = peerGroup.getConnectedPeers();    // snapshots
            // Prepare to send the transaction by adding a listener that'll be called when confidence changes.
            // Only bother with this if we might actually hear back:
            if (minConnections > 1)
                tx.getConfidence().addEventListener(new ConfidenceChange());
            // Bitcoin Core sends an inv in this case and then lets the peer request the tx data. We just
            // blast out the TX here for a couple of reasons. Firstly it's simpler: in the case where we have
            // just a single connection we don't have to wait for getdata to be received and handled before
            // completing the future in the code immediately below. Secondly, it's faster. The reason the
            // Bitcoin Core sends an inv is privacy - it means you can't tell if the peer originated the
            // transaction or not. However, we are not a fully validating node and this is advertised in
            // our version message, as SPV nodes cannot relay it doesn't give away any additional information
            // to skip the inv here - we wouldn't send invs anyway.
            int numConnected = peers.size();
            int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(peers.size() / 2.0)));
            numWaitingFor = (int) Math.ceil((peers.size() - numToBroadcastTo) / 2.0);
            Collections.shuffle(peers, random);
            peers = peers.subList(0, numToBroadcastTo);
            log.info("broadcastTransaction: We have {} peers, adding {} to the memory pool", numConnected, tx.getTxId());
            log.info("Sending to {} peers, will wait for {}, sending to: {}", numToBroadcastTo, numWaitingFor, Joiner.on(",").join(peers));
            for (Peer peer : peers) {
                try {
                    peer.sendMessage(tx);
                    // We don't record the peer as having seen the tx in the memory pool because we want to track only
                    // how many peers announced to us.
                } catch (Exception e) {
                    log.error("Caught exception sending to {}", peer, e);
                }
            }
            // If we've been limited to talk to only one peer, we can't wait to hear back because the
            // remote peer won't tell us about transactions we just announced to it for obvious reasons.
            // So we just have to assume we're done, at that point. This happens when we're not given
            // any peer discovery source and the user just calls connectTo() once.
            if (minConnections == 1) {
                peerGroup.removePreMessageReceivedEventListener(rejectionListener);
                future.set(tx);
            }
        }
    }

    private int numSeemPeers;
    private boolean mined;

    private class ConfidenceChange implements org.bitcoinj.core.TransactionConfidence.Listener {
        @Override
        public void onConfidenceChanged(TransactionConfidence conf, ChangeReason reason) {
            // The number of peers that announced this tx has gone up.
            int numSeenPeers = conf.numBroadcastPeers() + rejects.size();
            boolean mined = tx.getAppearsInHashes() != null;
            log.info("broadcastTransaction: {}:  TX {} seen by {} peers{}", reason, tx.getTxId(),
                    numSeenPeers, mined ? " and mined" : "");

            // Progress callback on the requested thread.
            invokeAndRecord(numSeenPeers, mined);

            if (numSeenPeers >= numWaitingFor || mined) {
                // We've seen the min required number of peers announce the transaction, or it was included
                // in a block. Normally we'd expect to see it fully propagate before it gets mined, but
                // it can be that a block is solved very soon after broadcast, and it's also possible that
                // due to version skew and changes in the relay rules our transaction is not going to
                // fully propagate yet can get mined anyway.
                //
                // Note that we can't wait for the current number of connected peers right now because we
                // could have added more peers after the broadcast took place, which means they won't
                // have seen the transaction. In future when peers sync up their memory pools after they
                // connect we could come back and change this.
                //
                // We're done! It's important that the PeerGroup lock is not held (by this thread) at this
                // point to avoid triggering inversions when the Future completes.
                log.info("broadcastTransaction: {} complete", tx.getTxId());
                peerGroup.removePreMessageReceivedEventListener(rejectionListener);
                conf.removeEventListener(this);
                future.set(tx);  // RE-ENTRANCY POINT
            }
        }
    }

    private void invokeAndRecord(int numSeenPeers, boolean mined) {
        synchronized (this) {
            this.numSeemPeers = numSeenPeers;
            this.mined = mined;
        }
        invokeProgressCallback(numSeenPeers, mined);
    }

    private void invokeProgressCallback(int numSeenPeers, boolean mined) {
        final ProgressCallback callback;
        Executor executor;
        synchronized (this) {
            callback = this.callback;
            executor = this.progressCallbackExecutor;
        }
        if (callback != null) {
            final double progress = Math.min(1.0, mined ? 1.0 : numSeenPeers / (double) numWaitingFor);
            checkState(progress >= 0.0 && progress <= 1.0, progress);
            try {
                if (executor == null)
                    callback.onBroadcastProgress(progress);
                else
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBroadcastProgress(progress);
                        }
                    });
            } catch (Throwable e) {
                log.error("Exception during progress callback", e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** An interface for receiving progress information on the propagation of the tx, from 0.0 to 1.0 */
    public interface ProgressCallback {
        /**
         * onBroadcastProgress will be invoked on the provided executor when the progress of the transaction
         * broadcast has changed, because the transaction has been announced by another peer or because the transaction
         * was found inside a mined block (in this case progress will go to 1.0 immediately). Any exceptions thrown
         * by this callback will be logged and ignored.
         */
        void onBroadcastProgress(double progress);
    }

    @Nullable private ProgressCallback callback;
    @Nullable private Executor progressCallbackExecutor;

    /**
     * Sets the given callback for receiving progress values, which will run on the user thread. See
     * {@link Threading} for details.  If the broadcast has already started then the callback will
     * be invoked immediately with the current progress.
     */
    public void setProgressCallback(ProgressCallback callback) {
        setProgressCallback(callback, Threading.USER_THREAD);
    }

    /**
     * Sets the given callback for receiving progress values, which will run on the given executor. If the executor
     * is null then the callback will run on a network thread and may be invoked multiple times in parallel. You
     * probably want to provide your UI thread or Threading.USER_THREAD for the second parameter. If the broadcast
     * has already started then the callback will be invoked immediately with the current progress.
     */
    public void setProgressCallback(ProgressCallback callback, @Nullable Executor executor) {
        boolean shouldInvoke;
        int num;
        boolean mined;
        synchronized (this) {
            this.callback = callback;
            this.progressCallbackExecutor = executor;
            num = this.numSeemPeers;
            mined = this.mined;
            shouldInvoke = numWaitingFor > 0;
        }
        if (shouldInvoke)
            invokeProgressCallback(num, mined);
    }
}
