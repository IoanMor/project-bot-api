package me.ivanmorozov.common.exception;

public class StockServiceException extends RuntimeException {
    public StockServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockServiceException(String msg) {
        super(msg);
    }
}