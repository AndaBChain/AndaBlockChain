package com.onets.core.exchange.shapeshift.data;

/**
 * @author Yu K.Q.
 */
public class ShapeShiftException extends Exception {
    public ShapeShiftException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShapeShiftException(Throwable cause) {
        super(cause);
    }

    public ShapeShiftException(String message) {
        super(message);
    }
}
