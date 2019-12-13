package org.rockyang.blockchain.exceptions;

/**
 * Encoding exception.
 * @author Wang HaiTian
 */
public class MessageEncodingException extends RuntimeException {
    public MessageEncodingException(String message) {
        super(message);
    }

    public MessageEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}