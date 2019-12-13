package com.onets.core.wallet;

import com.onets.core.coins.Value;

/**
 * @author Yu K.Q.
 * 钱包账户事件监听器
 */
public interface WalletAccountEventListener {

    /**
     * 新的余额
     * @param newBalance
     */
    void onNewBalance(Value newBalance);

    /**
     * 新的区块
     * @param pocket
     */
    void onNewBlock(WalletAccount pocket);

    /**
     * 交易改变
     * @param pocket
     * @param tx
     */
    void onTransactionConfidenceChanged(WalletAccount pocket, AbstractTransaction tx);

    /**
     * 交易广播失败
     * @param pocket
     * @param tx
     */
    void onTransactionBroadcastFailure(WalletAccount pocket, AbstractTransaction tx);

    /**
     * 交易广播成功
     * @param pocket
     * @param tx
     */
    void onTransactionBroadcastSuccess(WalletAccount pocket, AbstractTransaction tx);

    /**
     * 钱包改变
     * @param pocket
     */
    void onWalletChanged(final WalletAccount pocket);

    /**
     * 连接状态
     * @param connectivityStatus
     */
    void onConnectivityStatus(WalletConnectivityStatus connectivityStatus);
}
