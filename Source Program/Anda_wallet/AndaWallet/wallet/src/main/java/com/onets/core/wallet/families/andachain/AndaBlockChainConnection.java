package com.onets.core.wallet.families.andachain;

import com.onets.core.network.AddressStatus;
import com.onets.core.network.interfaces.BlockchainConnection;
import com.onets.core.wallet.TransactionWatcherWallet_Anda;

/**
 * 安达链连接接口
 */
public interface AndaBlockChainConnection extends BlockchainConnection<AndaTransaction> {
    void getUnspentTx(AddressStatus status, TransactionWatcherWallet_Anda listener);

    void getUnspentTx(AddressStatus status,
                      AndaTransactionEventListener listener);
}
