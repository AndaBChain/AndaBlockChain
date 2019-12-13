package com.onets.core.wallet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Yu K.Q.
 * 钱包交易抽象类
 */
public abstract class WalletTransaction<T extends AbstractTransaction> {
    public enum Pool {
        CONFIRMED, // in best chain
        PENDING, // a pending tx we would like to go into the best chain
    }

    //交易定义
    private final T transaction;
    //池定义
    private final Pool pool;

    /**
     * 钱包交易--有参构造
     * @param pool
     * @param transaction
     */
    public WalletTransaction(Pool pool, T transaction) {
        this.pool = checkNotNull(pool);
        this.transaction = transaction;
    }

    /**
     * 获取交易
     * @return
     */
    public T getTransaction() {
        return transaction;
    }

    /**
     * 获取池
     * @return
     */
    public Pool getPool() {
        return pool;
    }
}
