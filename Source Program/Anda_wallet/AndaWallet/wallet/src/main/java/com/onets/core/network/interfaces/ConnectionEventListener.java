package com.onets.core.network.interfaces;

/**
 * 连接监听接口
 * @author Yu K.Q.
 * 接口调用是在WalletAccount
 */
public interface ConnectionEventListener {
    /**
     * 连接
     * @param blockchainConnection
     */
    void onConnection(BlockchainConnection blockchainConnection);

    /**
     * 未连接
     */
    void onDisconnect();
}
