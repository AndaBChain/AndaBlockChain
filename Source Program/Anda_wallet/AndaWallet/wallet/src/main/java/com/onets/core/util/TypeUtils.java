package com.onets.core.util;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.ValueType;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.WalletAccount;

/**
 * @author Yu K.Q.
 * 类型工具类
 */
public class TypeUtils {
    public static boolean is(CoinType myType, WalletAccount other) {
        return other != null && myType.equals(other.getCoinType());
    }

    public static boolean is(CoinType myType, ValueType otherType) {
        return otherType != null && myType.equals(otherType);
    }

    public static boolean is(CoinType myType, AbstractAddress address) {
        return address != null && myType.equals(address.getType());
    }
}
