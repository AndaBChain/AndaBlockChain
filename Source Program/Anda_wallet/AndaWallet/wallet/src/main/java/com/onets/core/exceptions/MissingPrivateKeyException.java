package com.onets.core.exceptions;

/**
 * @author Yu K.Q.
 */
public class MissingPrivateKeyException extends Exception {
    public MissingPrivateKeyException(String message) {
        super(message);
    }

    public MissingPrivateKeyException(Throwable cause) {
        super(cause);
    }
}
