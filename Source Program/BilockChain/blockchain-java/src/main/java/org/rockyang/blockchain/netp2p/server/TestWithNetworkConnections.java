package org.rockyang.blockchain.netp2p.server;

import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;
import org.bitcoinj.core.*;
import org.bitcoinj.net.*;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;

import com.google.common.util.concurrent.SettableFuture;
import org.rockyang.blockchain.testing.InboundMessageQueuer;

import javax.annotation.Nullable;
import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility class that makes it easy to work with mock NetworkConnections
 * 使用模拟网络连接更容易的实用程序类。.
 */
//使用网络连接进行测试
public class TestWithNetworkConnections {
    public static final int PEER_SERVERS = 5;
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected Context context;
    protected BlockStore blockStore;
    protected BlockChain blockChain;
    protected Wallet wallet;
    protected ECKey key;
    protected Address address;
    protected SocketAddress socketAddress;

    private NioServer[] peerServers = new NioServer[PEER_SERVERS];
    private final ClientConnectionManager channels;
    protected final BlockingQueue<InboundMessageQueuer> newPeerWriteTargetQueue = new LinkedBlockingQueue<>();

    public enum ClientType {
        NIO_CLIENT_MANAGER,
        BLOCKING_CLIENT_MANAGER,
        NIO_CLIENT,
        BLOCKING_CLIENT
    }
    private final org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType clientType;
    //使用网络连接进行测试
    public TestWithNetworkConnections(org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType clientType) {
        this.clientType = clientType;
        if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.NIO_CLIENT_MANAGER)
            channels = new NioClientManager();
        else if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.BLOCKING_CLIENT_MANAGER)
            channels = new BlockingClientManager();
        else
            channels = null;
    }

    public void setUp() throws Exception {
        setUp(new MemoryBlockStore(UNITTEST));
    }
    //设置
    public void setUp(BlockStore blockStore) throws Exception {
        BriefLogFormatter.init();
        Context.propagate(new Context(UNITTEST, 100, Coin.ZERO, false));
        this.blockStore = blockStore;
        // Allow subclasses to override the wallet object with their own
        // 允许子类用自己的类覆盖wallet对象。
        if (wallet == null) {
            // Reduce the number of keys we need to work with to speed up these tests
            // 减少我们需要使用的键的数量来加速这些测试。.
            KeyChainGroup kcg = KeyChainGroup.builder(UNITTEST).lookaheadSize(4).lookaheadThreshold(2)
                    .fromRandom(Script.ScriptType.P2PKH).build();
            wallet = new Wallet(UNITTEST, kcg);
            key = wallet.freshReceiveKey();
            address = LegacyAddress.fromKey(UNITTEST, key);
        }
        blockChain = new BlockChain(UNITTEST, wallet, blockStore);

        startPeerServers();
        if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.NIO_CLIENT_MANAGER || clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.BLOCKING_CLIENT_MANAGER) {
            channels.startAsync();
            channels.awaitRunning();
        }

        socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 1111);
    }

    //启动对等服务器
    protected void startPeerServers() throws IOException {
        for (int i = 0 ; i < 5 ; i++) {
            /*if (i!=3) {*/
            startPeerServer(i);

        }
    }
    //启动对等服务器
    protected void startPeerServer(int i) throws IOException {
        peerServers[i] = new NioServer(new StreamConnectionFactory() {
            @Nullable
            @Override
            public StreamConnection getNewConnection(InetAddress inetAddress, int port) {
                return new InboundMessageQueuer(UNITTEST) {
                    @Override
                    public void connectionClosed() {
                    }
                    @Override
                    public void connectionOpened() {
                        newPeerWriteTargetQueue.offer(this);
                    }
                };
            }
        }, new InetSocketAddress(InetAddress.getLoopbackAddress(), 2000 +i ));
        peerServers[i].startAsync();
        peerServers[i].awaitRunning();
    }

    public void tearDown() throws Exception {
        stopPeerServers();
    }

    protected void stopPeerServers() {
        for (int i = 0 ; i < PEER_SERVERS ; i++)
            stopPeerServer(i);
    }

    protected void stopPeerServer(int i) {
        peerServers[i].stopAsync();
        peerServers[i].awaitTerminated();
    }

    protected InboundMessageQueuer connect(Peer peer, VersionMessage versionMessage) throws Exception {
        checkArgument(versionMessage.hasBlockChain());
        final AtomicBoolean doneConnecting = new AtomicBoolean(false);
        final Thread thisThread = Thread.currentThread();
        peer.addDisconnectedEventListener(new PeerDisconnectedEventListener() {
            @Override
            public void onPeerDisconnected(Peer p, int peerCount) {
                synchronized (doneConnecting) {
                    if (!doneConnecting.get())
                        thisThread.interrupt();
                }
            }
        });
        if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.NIO_CLIENT_MANAGER || clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.BLOCKING_CLIENT_MANAGER)
            channels.openConnection(new InetSocketAddress(InetAddress.getLoopbackAddress(), 2000), peer);
        else if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.NIO_CLIENT)
            new NioClient(new InetSocketAddress(InetAddress.getLoopbackAddress(), 2000), peer, 100);
        else if (clientType == org.rockyang.blockchain.testing.TestWithNetworkConnections.ClientType.BLOCKING_CLIENT)
            new BlockingClient(new InetSocketAddress(InetAddress.getLoopbackAddress(), 2000), peer, 100, SocketFactory.getDefault(), null);
        else
            throw new RuntimeException();
        // Claim we are connected to a different IP that what we really are, so tx confidence broadcastBy sets work
        // 声称我们连接到一个不同的IP，所以tx信心广播集工作.
        InboundMessageQueuer writeTarget = newPeerWriteTargetQueue.take();
        writeTarget.peer = peer;
        // Complete handshake with the peer - send/receive version(ack)s, receive bloom filter
        // 完成与对等端发送/接收版本(ack)的握手，接收bloom过滤器。
        checkState(!peer.getVersionHandshakeFuture().isDone());
        writeTarget.sendMessage(versionMessage);
        writeTarget.sendMessage(new VersionAck());
        try {
            checkState(writeTarget.nextMessageBlocking() instanceof VersionMessage);
            checkState(writeTarget.nextMessageBlocking() instanceof VersionAck);
            peer.getVersionHandshakeFuture().get();
            synchronized (doneConnecting) {
                doneConnecting.set(true);
            }
            Thread.interrupted(); // Clear interrupted bit in case it was set before we got into the CS
            // 。清除中断位，以防在进入CS之前设置好
        } catch (InterruptedException e) {
            // We were disconnected before we got back version/verack
            // 我们在返回版本/verack之前断开了连接
        }
        return writeTarget;
    }

    protected void closePeer(Peer peer) throws Exception {
        peer.close();
    }

    protected void inbound(InboundMessageQueuer peerChannel, Message message) {
        peerChannel.sendMessage(message);
    }

    private void outboundPingAndWait(final InboundMessageQueuer p, long nonce) throws Exception {
        // Send a ping and wait for it to get to the other side
        // 发送一个ping信号，等待它到达另一边。
        SettableFuture<Void> pingReceivedFuture = SettableFuture.create();
        p.mapPingFutures.put(nonce, pingReceivedFuture);
        p.peer.sendMessage(new Ping(nonce));
        pingReceivedFuture.get();
        p.mapPingFutures.remove(nonce);
    }

    private void inboundPongAndWait(final InboundMessageQueuer p, final long nonce) throws Exception {
        // Receive a ping (that the Peer doesn't see) and wait for it to get through the socket
        // 接收一个ping(对等方看不到)并等待它通过套接字。
        final SettableFuture<Void> pongReceivedFuture = SettableFuture.create();
        PreMessageReceivedEventListener listener = new PreMessageReceivedEventListener() {
            @Override
            public Message onPreMessageReceived(Peer p, Message m) {
                if (m instanceof Pong && ((Pong) m).getNonce() == nonce) {
                    pongReceivedFuture.set(null);
                    return null;
                }
                return m;
            }
        };
        p.peer.addPreMessageReceivedEventListener(Threading.SAME_THREAD, listener);
        inbound(p, new Pong(nonce));
        pongReceivedFuture.get();
        p.peer.removePreMessageReceivedEventListener(listener);
    }

    protected void pingAndWait(final InboundMessageQueuer p) throws Exception {
        final long nonce = (long) (Math.random() * Long.MAX_VALUE);
        // Start with an inbound Pong as pingAndWait often happens immediately after an inbound() call, and then wants
        // to wait on an outbound message, so we do it in the same order or we see race conditions
        // 从入站Pong开始，因为pingAndWait通常在入站()调用之后立即发生，然后希望等待出站消息，
        // 所以我们按照相同的顺序执行，或者查看竞争条件。
        inboundPongAndWait(p, nonce);
        outboundPingAndWait(p, nonce);
    }

    protected Message outbound(InboundMessageQueuer p1) throws Exception {
        pingAndWait(p1);
        return p1.nextMessage();
    }

    protected Message waitForOutbound(InboundMessageQueuer ch) throws InterruptedException {
        return ch.nextMessageBlocking();
    }

    protected Peer peerOf(InboundMessageQueuer ch) {
        return ch.peer;
    }
}
