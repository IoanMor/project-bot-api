package me.ivanmorozov.common.kafka;

public class MessageTypes {
    // Типы для запросов
    public static final String CHAT_REGISTER = "CHAT_REGISTER";
    public static final String CHAT_CHECK = "CHAT_CHECK";
    public static final String LINK_SUBSCRIBE = "LINK_SUBSCRIBE";


    //Ответы
    public static final String SUCCESS = "SUCCESS";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String CREATED = "CREATED";
    public static final String NOT_CREATED = "NOT_CREATED";
    public static final String EXIST_CHAT = "CHAT_IS_EXIST";

    // Ошибки
    public static final String ERROR = "ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";

    // Состояния
    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
}
