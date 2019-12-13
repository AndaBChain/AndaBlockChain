package com.onets.core.wallet;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;

import java.util.List;

/**
 * 比特币监听类
 */
public class BitcoinWalletListener implements WalletEventListener {

	@Override
	public void onKeysAdded(List<ECKey> keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCoinsReceived(org.bitcoinj.core.Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		final Coin value = tx.getValueSentToMe(wallet);

		// String address =
		// wallet.currentReceiveKey().toAddress(params).toString();

		System.out.println("tx" + value.toFriendlyString() + ": " + tx.toString());
		System.out.println("Previous balance is " + prevBalance.toFriendlyString());
		System.out.println("New estimated balance is " + newBalance.toFriendlyString());
		System.out.println("Coin received, wallet balance is :" + wallet.getBalance());
		Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onSuccess(Transaction result) {
				// TODO Auto-generated method stub
				System.out.println("Transaction confirmed, wallet balance is :" + wallet.getBalance());

			}
		});
		
	}

	@Override
	public void onCoinsSent(org.bitcoinj.core.Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReorganize(org.bitcoinj.core.Wallet wallet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTransactionConfidenceChanged(org.bitcoinj.core.Wallet wallet, Transaction tx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWalletChanged(org.bitcoinj.core.Wallet wallet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
		// TODO Auto-generated method stub
		
	}

}
