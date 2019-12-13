package org.rockyang.blockchain.exceptions;

/**
 * Encoding exception.
 * @author Wang HaiTian
 */
public class MessageDecodingException extends RuntimeException {
    public MessageDecodingException(String message) {
        super(message);
    }

    public MessageDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
