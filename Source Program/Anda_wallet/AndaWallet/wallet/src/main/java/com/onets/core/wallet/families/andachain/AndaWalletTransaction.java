package com.onets.core.wallet.families.andachain;

import com.onets.core.wallet.WalletTransaction;

/**
 * @author Yu K.Q.
 * 安达钱包交易
 */
public class AndaWalletTransaction extends WalletTransaction<AndaTransaction> {
    public AndaWalletTransaction(Pool pool, AndaTransaction transaction) {
        super(pool, transaction);
    }
}
