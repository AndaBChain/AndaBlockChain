package com.onets.core.wallet.families.ethereum.utils;

import com.onets.core.coins.nxt.NxtException;

import java.math.BigInteger;

/**
 * 以太坊交易接口
 */
public interface Transaction extends Comparable<Transaction> {

    public static interface Builder {
        //目标地址id
        Builder recipientId(long recipientId);

        Transaction build() throws NxtException.NotValidException;

    }

    void setHeight(int height);
    int getHeight();

    void setConfirmations(int confirmations);
    int getConfirmations();

    long getId();

   // long getSenderId();

    long getRecipientId();

    int getTimestamp();

    BigInteger getAmountETH();

    String getFullHash();

    void sign(byte[] privateKey);

    byte[] getBytes();

    int getBlockHeight();


}
