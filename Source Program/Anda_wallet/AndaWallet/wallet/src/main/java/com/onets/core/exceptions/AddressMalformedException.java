package com.onets.core.exceptions;

/**
 * 地址畸形异常
 * @author Yu K.Q.
 */
public class AddressMalformedException extends Exception {
    public AddressMalformedException(String message) {
        super(message);
    }

    public AddressMalformedException(Throwable cause) {
        super(cause);
    }
}
