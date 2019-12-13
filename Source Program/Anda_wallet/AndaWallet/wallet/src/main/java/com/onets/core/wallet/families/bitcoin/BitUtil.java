package com.onets.core.wallet.families.bitcoin;

import android.text.TextUtils;

import com.onets.wallet.Constants;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BitUtil {
    public static final String passphrase = "";

    //比特币发送交易
    public static void send(Wallet wallet, String recipientAddress, String amount){
        //获取当前比特币网络
        NetworkParameters params = getParams();
        //将字符串转为比特币地址
        Address targetAddress = null;
        try {
            targetAddress = new Address(params,recipientAddress);
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

        SPVBlockStore blockStore = null;
        try {
            blockStore = new SPVBlockStore(params, getBlockFile());
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }
        BlockChain chain = null;
        try {
            chain = new BlockChain(params, wallet, blockStore);
            PeerGroup peerGroup = new PeerGroup(params, chain);
            try {
                Wallet.SendResult result = wallet.sendCoins(peerGroup, targetAddress, Coin.parseCoin(amount));
                try {
                    Transaction transaction = result.broadcastComplete.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return;
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
            }
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }

    }

    public static String send(WalletAppKit walletAppKit, String recipientAddress, String amount){
        NetworkParameters params = getParams();
        String err = "";
        if (TextUtils.isEmpty(recipientAddress) || recipientAddress.equals("Scan recipient QR")){
            err = "Select recipient";
            return err;
        }
        if (TextUtils.isEmpty(amount) | Double.parseDouble(amount) <= 0){
            err = "Select valid amount";
            return err;
        }
        if (walletAppKit.wallet().getBalance().isLessThan(Coin.parseCoin(amount))){
            err = "You got not enough coins";
            return err;
        }
        Wallet.SendRequest request = Wallet.SendRequest.to(Address.fromP2SHHash(params,recipientAddress.getBytes()), Coin.parseCoin(amount));
        try {
            walletAppKit.wallet().completeTx(request);
            walletAppKit.wallet().commitTx(request.tx);
            //walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
            return "";
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        }
        return err;
    }

    //获取区块文件
    public static File getBlockFile(){
        File file = new File("/tmp/bitcoin-blocks");
        if (!file.exists()){
            try {
                boolean newFile = file.createNewFile();
                if (newFile){
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 主网 or 测试网
     * @return TEST=true,测试网；反之，主网
     */
    public static NetworkParameters getParams(){
        return Constants.TEST ? MainNetParams.get() : TestNet3Params.get();
    }
}
