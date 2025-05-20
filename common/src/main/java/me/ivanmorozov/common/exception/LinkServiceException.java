package me.ivanmorozov.common.exception;

public class LinkServiceException extends RuntimeException {
    public LinkServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinkServiceException(String msg) {
        super(msg);
    }
}