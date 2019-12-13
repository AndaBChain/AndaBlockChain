package com.onets.core.network.interfaces;

import com.onets.core.network.AddressStatus;
import com.onets.core.wallet.AbstractAddress;

import org.bitcoinj.core.Sha256Hash;

import java.util.List;

import javax.annotation.Nullable;

/**
 * 区块链连接
 * @author Yu K.Q.
 * 接口方法实现是在NxtServerClient
 * 接口方法实现是在EthereumServerClient
 * 接口方法实现是在RippleServerClient
 * 接口的调用是在AndaBlockChainConnection 、 BitBlockchainConnection
 */
public interface BlockchainConnection<T> {

    /**
     * 获取区块
     * @param height
     * @param listener
     */
    void getBlock(int height, TransactionEventListener<T> listener);

    /**
     * 提交到区块链
     * @param listener
     */
    void subscribeToBlockchain(final TransactionEventListener<T> listener);

    /**
     * 提交到地址
     * @param addresses
     * @param listener
     */
    void subscribeToAddresses(List<AbstractAddress> addresses,
                              TransactionEventListener<T> listener);

    /**
     * 获取历史交易
     * @param status
     * @param listener
     */
    void getHistoryTx(AddressStatus status, TransactionEventListener<T> listener);

    /**
     * 获取交易
     * @param txHash
     * @param listener
     */
    void getTransaction(Sha256Hash txHash, TransactionEventListener<T> listener);

    /**
     * 广播交易
     * @param tx
     * @param listener
     */
    void broadcastTx(final T tx, final TransactionEventListener<T> listener);

    /**
     * 广播交易同步
     * @param tx
     * @return
     */
    boolean broadcastTxSync(final T tx);

    /**
     * ping检测
     * @param versionString
     */
    void ping(@Nullable String versionString);

    /**
     * 添加事件监听
     * @param listener
     */
    void addEventListener(ConnectionEventListener listener);

    /**
     * 重置连接
     */
    void resetConnection();

    /**
     * 开始同步
     */
    void startAsync();

    /**
     * 停止同步
     */
    void stopAsync();

    /**
     * 判断连接是否活动状态
     * @return
     */
    boolean isActivelyConnected();


}
