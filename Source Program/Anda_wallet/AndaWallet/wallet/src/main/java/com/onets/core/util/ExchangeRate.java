package com.onets.core.util;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;

import org.bitcoinj.core.Coin;

import java.io.Serializable;

/**
 * 汇率接口
 * @author Yu K.Q.
 *
 */
public interface ExchangeRate extends Serializable {
    @Deprecated
    Value convert(CoinType type, Coin coin);

    /**
     * Convert from one value to another
     */
    Value convert(Value convertingValue);

    ValueType getOtherType(ValueType type);

    ValueType getSourceType();
    ValueType getDestinationType();

    boolean canConvert(ValueType type1, ValueType type2);
}
