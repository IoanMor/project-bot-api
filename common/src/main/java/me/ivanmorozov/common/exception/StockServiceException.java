package me.ivanmorozov.common.exception;

public class StockServiceException extends RuntimeException {
    public StockServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}