package com.onets.core.wallet.families.bitcoin;

import com.onets.core.network.AddressStatus;
import com.onets.core.network.interfaces.BlockchainConnection;

/**
 * @author Yu K.Q.
 * 比特币连接接口
 */
public interface BitBlockchainConnection extends BlockchainConnection<BitTransaction> {
    void getUnspentTx(AddressStatus status, BitTransactionEventListener listener);
}
