package com.onets.core.coins;

/**
 * 自定义货币
 * Created by Hasee on 2018/1/12.
 * 接口方法实现是在Value
 */

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Classes implementing this interface represent a monetary value, such as a Bitcoin or fiat amount.
 * 实现这个接口的类代表一个货币值，比如比特币或法定数量
 */
public interface MyMonetary extends Serializable {
    /*最小单位指数*/
    int smallestUnitExponent();

    /*获得货币单位数*/
    BigInteger getBigValue();

    /*符号*/
    int signum();
}
