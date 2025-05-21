package me.ivanmorozov.common.exception;

public class SendChatException extends RuntimeException{
    public SendChatException(String msg, Throwable throwable) {
        super(msg,throwable);
    }

    public SendChatException(String msg) {
        super(msg);
    }
}
