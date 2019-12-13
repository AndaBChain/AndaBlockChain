package com.onets.core.wallet.families.bitcoin;

import com.onets.core.wallet.WalletTransaction;

/**
 * @author Yu K.Q.
 * 比特币钱包交易
 */
public class BitWalletTransaction extends WalletTransaction<BitTransaction> {
    public BitWalletTransaction(Pool pool, BitTransaction transaction) {
        super(pool, transaction);
    }
}
