package com.onets.core.wallet;

import com.onets.core.coins.CoinType;

import java.io.Serializable;

/**
 * 地址抽象类
 * @author Yu K.Q.
 */
public interface AbstractAddress extends Serializable {
    CoinType getType();
    String toString();
    long getId();
}
