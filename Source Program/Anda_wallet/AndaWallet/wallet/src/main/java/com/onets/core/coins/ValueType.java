package com.onets.core.coins;

import com.onets.core.util.MonetaryFormat;

import org.bitcoinj.core.Coin;

import java.io.Serializable;

/**
 * @author Yu K.Q.
 * 接口方法实现是在CoinType：无getId()
 * 接口方法实现是在FiatType
 */
public interface ValueType extends Serializable {

    /**
     * 获取币ID
     */
    String getId();

    /**
     * 获取币名
     */
    String getName();

    /**
     * 获取符号
     */
    String getSymbol();

    /**
     * 获取单位指数
     */
    int getUnitExponent();

    /**
     * Typical 1 coin value, like 1 Bitcoin, 1 Peercoin or 1 Dollar
     */
    Value oneCoin();

    /**
     * Get the minimum valid amount that can be sent a.k.a. dust amount or minimum input
     */
    Value getMinNonDust();

    Value value(Coin coin);

    Value value(long units);

    /**
     * 获取货币格式
     */
    MonetaryFormat getMonetaryFormat();
    MonetaryFormat getPlainFormat();

    boolean equals(ValueType obj);

    Value value(String string);
}
