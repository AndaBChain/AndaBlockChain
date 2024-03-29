package com.onets.stratumj.messages;

/**
 * 信息异常
 * @author Yu K.Q.
 */
public class MessageException extends Exception {
    public MessageException(String error, ResultMessage result) {
        super(error + ": " + result);
    }

    public MessageException(String errorMessage, String faultyRequest) {
        super(errorMessage + ": " + faultyRequest);
    }
}
