package com.onets.core.wallet.families.bitcoin;

import com.onets.core.network.AddressStatus;
import com.onets.core.network.ServerClient.UnspentTx;
import com.onets.core.network.interfaces.TransactionEventListener;

import java.util.List;

/**
 * @author Yu K.Q.
 * 比特币交易监听事件接口
 */
public interface BitTransactionEventListener extends TransactionEventListener<BitTransaction> {
    void onUnspentTransactionUpdate(AddressStatus status, List<UnspentTx> UnspentTxes);
}
