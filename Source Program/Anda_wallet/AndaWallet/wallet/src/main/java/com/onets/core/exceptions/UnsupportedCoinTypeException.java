package com.onets.core.exceptions;

import com.onets.core.coins.CoinType;

/**
 * @author Yu K.Q.
 */
public class UnsupportedCoinTypeException extends RuntimeException {
    public UnsupportedCoinTypeException(CoinType type) {
        super("Unsupported coin type: " + type);
    }

    public UnsupportedCoinTypeException(String message) {
        super(message);
    }
}
