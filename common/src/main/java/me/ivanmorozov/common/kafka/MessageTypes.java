package me.ivanmorozov.common.kafka;

public class MessageTypes {
    // Запросов
    public static final String CHAT_REGISTER = "CHAT_REGISTER";
    public static final String CHAT_CHECK = "CHAT_CHECK";
    public static final String LINK_SUBSCRIBE = "LINK_SUBSCRIBE";
    public static final String LINK_UNSUBSCRIBE = "LINK_UNSUBSCRIBE";
    public static final String LINK_GET_ALL_SUBS = "LINK_GET_ALL_SUBS";
    public static final String STOCK_SUBSCRIBE = "STOCK_SUBSCRIBE";
    public static final String STOCK_UNSUBSCRIBE = "STOCK_UNSUBSCRIBE";
    public static final String STOCK_GET_ALL_SUBS = "STOCK_GET_ALL_SUBS";



    // Ответы
    public static final String SUCCESS = "SUCCESS";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String CREATED = "CREATED";
    public static final String NOT_CREATED = "NOT_CREATED";


    public static final String EXIST_CHAT_REGISTER = "CHAT_IS_EXIST_REGISTER";
    public static final String EXIST_CHAT_CHECK = "CHAT_IS_EXIST";
    public static final String EXIST_SUBSCRIBE_LINK = "LINK_IS_SUBSCRIBE";
    public static final String EXIST_SUBSCRIBE_STOCK = "STOCK_IS_SUBSCRIBE";

    public static final String UNSUBSCRIBE_RESULT_LINK = "OPERATION_RESULT_LINK";
    public static final String UNSUBSCRIBE_RESULT_STOCK = "OPERATION_RESULT_STOCK";
    public static final String GET_ALL_LINKS = "GET_ALL_LINKS";
    public static final String GET_ALL_STOCK = "GET_ALL_STOCK";
    public static final String STOCK_SHEDULED_MSG = "STOCK_SHEDULED_MSG";




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
