package me.ivanmorozov.common.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(String msg) {
        super(msg);
    }
}

