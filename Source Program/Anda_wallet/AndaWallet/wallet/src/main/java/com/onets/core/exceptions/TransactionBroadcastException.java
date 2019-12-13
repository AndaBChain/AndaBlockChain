package com.onets.core.exceptions;

/**
 * Created by Yu K.Q. on 29/9/2015.
 */
public class TransactionBroadcastException extends Exception {
    public TransactionBroadcastException(String message) {
        super(message);
    }

    public TransactionBroadcastException(Throwable cause) {
        super(cause);
    }
}
