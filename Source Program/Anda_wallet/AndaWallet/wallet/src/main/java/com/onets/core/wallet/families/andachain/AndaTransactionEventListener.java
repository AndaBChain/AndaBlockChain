package com.onets.core.wallet.families.andachain;

import com.onets.core.network.AddressStatus;
import com.onets.core.network.ServerClient.UnspentTx;
import com.onets.core.network.interfaces.TransactionEventListener;

import java.util.List;

/**
 * @author Yu K.Q.
 * 安达交易监听事件接口
 */
public interface AndaTransactionEventListener extends TransactionEventListener<AndaTransaction> {
    void onUnspentTransactionUpdate(AddressStatus status, List<UnspentTx> UnspentTxes);
}
