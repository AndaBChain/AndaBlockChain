package com.onets.core.network.interfaces;

import com.onets.core.network.AddressStatus;
import com.onets.core.network.BlockHeader;
import com.onets.core.network.ServerClient.HistoryTx;

import java.util.List;

/**
 * 交易监听接口
 * @author Yu K.Q.
 */
public interface TransactionEventListener<T> {

    /**
     * 新的区块
     * @param header blockheader
     */
    void onNewBlock(BlockHeader header);

    /**
     * 区块更新
     * @param header blockheader
     */
    void onBlockUpdate(BlockHeader header);

    /**
     * 地址状态更新
     * @param status
     */
    void onAddressStatusUpdate(AddressStatus status);

    /**
     * 交易历史
     * @param status
     * @param historyTxes
     */
    void onTransactionHistory(AddressStatus status, List<HistoryTx> historyTxes);

    /**
     * 交易更新
     * @param transaction
     */
    void onTransactionUpdate(T transaction);

    /**
     * 交易广播
     * @param transaction
     */
    void onTransactionBroadcast(T transaction);

    /**
     * 交易广播错误
     * @param transaction
     */
    void onTransactionBroadcastError(T transaction);
}
