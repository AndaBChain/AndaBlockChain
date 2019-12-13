package org.rockyang.blockchain.netp2p.server;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.net.NioServer;
import org.bitcoinj.net.StreamConnection;
import org.bitcoinj.net.StreamConnectionFactory;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.testing.InboundMessageQueuer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node0 {
    public static final int PEER_SERVERS = 5;
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();

    public static void main(String[] args) throws IOException {

        startPeerServer(1);

    }
    //启动对等服务器
    protected void startPeerServers() throws IOException {

    }
    //启动对等服务器
    protected static void startPeerServer(int i) throws IOException {
        NioServer[] peerServers = new NioServer[PEER_SERVERS];
        BlockingQueue<InboundMessageQueuer> newPeerWriteTargetQueue = new LinkedBlockingQueue<>();

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
}
