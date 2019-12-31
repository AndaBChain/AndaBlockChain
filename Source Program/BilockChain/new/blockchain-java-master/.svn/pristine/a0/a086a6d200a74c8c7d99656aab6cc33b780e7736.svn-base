package com.aizone.blockchain.utils;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;

import com.google.common.util.concurrent.MoreExecutors;
/**
 * 比特币工具类
 * @author YAY
 *
 */
public class BitcoinJUtil {
	public static NetworkParameters params = TestNet3Params.get();
	public static Wallet bit_wallet = new org.bitcoinj.core.Wallet(params);
	public static PeerGroup peerGroup = new PeerGroup(params);
	/**
	 * 广播交易
	 * @param tx
	 */
	public static void broadcastTransactionUtil(Transaction tx) {
		Wallet.SendResult result = new Wallet.SendResult();
		result.tx = tx;
		result.broadcastComplete = peerGroup.broadcastTransaction(result.tx);
		result.broadcastComplete.addListener(new Runnable() {
			@Override
			public void run() {
				String tx = "";
				System.out.println("交易费用:" + result.tx.getFee());
				for (TransactionInput list : result.tx.getInputs()) {
					System.out.println("input:" + list.getFromAddress());
					tx = tx + "input_Address:" + list.getFromAddress() + ",";
				}
				for (TransactionOutput list : result.tx.getOutputs()) {
					System.out.println("getAddressFromP2PKHScript___output:" + list.getAddressFromP2PKHScript(params));
					tx = tx + "Output_Address:" + list.getAddressFromP2PKHScript(params) + ",";
				}
				tx = tx + result.tx.toString();
				System.out.println(result.tx.toString());
				System.out.println("Coins Sent! Transaction hash is " + result.tx.getHashAsString());
				System.out.println("发送成功");

				}
		}, MoreExecutors.sameThreadExecutor());
	}
	/**
	 * 启动peerGroup
	 */
	public static void startPeerGroup() {
		peerGroup.addPeerDiscovery(new DnsDiscovery(params));
		peerGroup.addWallet(bit_wallet);
		peerGroup.startAsync();
	}
	/**
	 * 停止peerGroup
	 */
	public static void stopPeerGroup(){
		peerGroup.stopAsync();
	}

}
