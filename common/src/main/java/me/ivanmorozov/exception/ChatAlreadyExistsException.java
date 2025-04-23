package me.ivanmorozov.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(long chatId) {
        super("Chat " + chatId + " already exists");
    }
}

