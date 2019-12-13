package com.onets.core.wallet.families.andachain;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.util.TypeUtils;
import com.onets.core.wallet.SendRequest;
import com.onets.wallet.util.HttpClientPost;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 * 安达发送请求
 */
public class AndaSendRequest extends SendRequest<AndaTransaction> {
    public AndaSendRequest(CoinType type) {
        super(type);
    }
    /**
     * 测试用，安卓前端页面请求服务器
     * 提交交易请求
     * @param amount
     * @return
     */
    public static boolean to(String sender , String recipient, String publicKey, String sign, String txHash , String timestamp, double amount, String data){
        HttpClientPost httpclient = new HttpClientPost();
        boolean result = false;
        result = httpclient.getNewTransactionPost(sender,recipient,amount,publicKey,sign,timestamp,txHash,data);
        return result;
    }

    /**
     * 测试用，请求服务器
     * 发送钱包地址信息
     */
    public static boolean to(String address ,String publicKey, String privateKey){
        HttpClientPost httpclient = new HttpClientPost();
        return  httpclient.commitAccountInfoPost(address,publicKey,privateKey);

    }

    /**
     * 接口获取账户余额
     */
    public static double to(String address){
        HttpClientPost httpclient = new HttpClientPost();
        double balance = httpclient.getAccountBalance(address);
        return balance;
    }

    /**
     * <p>Creates a new SendRequest to the given address for the given value.</p>
     *  安达发送请求
     *  为给定值创建一个新的SendRequest到给定地址
     * <p>Be very careful when value is smaller than {@link Transaction#MIN_NONDUST_OUTPUT} as the transaction will
     * likely be rejected by the network in this case.</p>
     */
    public static AndaSendRequest to(AndaAddress destination, Value amount) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        checkState(TypeUtils.is(destination.getType(), amount.type), "Incompatible sending amount type");
        checkTypeCompatibility(destination.getType());

        AndaSendRequest req = new AndaSendRequest(destination.getType());

        Transaction tx = new Transaction(req.type);
        tx.addOutput(amount.toAndaCoin(), destination);

        req.tx = new AndaTransaction(tx);

        return req;
    }

    /**
     * 安达空钱包发送请求
     * @param destination
     * @return
     */
    public static AndaSendRequest emptyWallet(AndaAddress destination) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        checkTypeCompatibility(destination.getType());

        AndaSendRequest req = new AndaSendRequest(destination.getType());

        Transaction tx = new Transaction(req.type);
        tx.addOutput(Coin.ZERO, destination);
        req.tx = new AndaTransaction(tx);
        req.emptyWallet = true;

        return req;
    }

    /**
     * 检查兼容的币类型
     * @param type
     */
    private static void checkTypeCompatibility(CoinType type) {
        // Only Bitcoin family coins are supported
        if (!(type instanceof AndaFamily)) {
            throw new RuntimeException("Unsupported type: " + type);
        }
    }
}
