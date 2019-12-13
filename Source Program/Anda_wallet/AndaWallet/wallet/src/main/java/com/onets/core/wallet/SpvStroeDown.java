package com.onets.core.wallet;

import com.onets.wallet.Constants;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.SPVBlockStore;

public class SpvStroeDown {
    /**
     * 比特币
     */
    public static SPVBlockStore blockStore;
    public static BlockChain blockChain;
    public static PeerGroup peerGroup = null;
    public static org.bitcoinj.core.Wallet bit_wallet;
    public static NetworkParameters params = Constants.NETWORK_PARAMETERS;


    public static void SPVstoreDown() {
                bit_wallet = new org.bitcoinj.core.Wallet(params);
                peerGroup = new PeerGroup(params);
                peerGroup.addPeerDiscovery(new DnsDiscovery(params));
                peerGroup.addWallet(bit_wallet);
                peerGroup.startAsync();
    }

    /**
     * 获取
     * @return
     */

    public org.bitcoinj.core.Wallet getBtcWallet(){
        return bit_wallet;
    }
    public PeerGroup getPeerGroup(){
        return peerGroup;
    }
    public NetworkParameters getParams(){return params;}
    public static void SpvDownStop(){
        peerGroup.stopAsync();
    }
}
