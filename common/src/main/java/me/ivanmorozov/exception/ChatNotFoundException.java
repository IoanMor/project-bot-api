package me.ivanmorozov.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(long chatId) {
        super("Chat " + chatId + " not found");
    }
}
