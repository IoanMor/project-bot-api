package me.ivanmorozov.common.endpoints;

public class ScrapperEndpoints {
    public static final String TG_CHAT_URI = "http://localhost:9030";

    public static final String TG_CHAT = "/tg-chat";
    public static final String TG_CHAT_REGISTER = TG_CHAT + "/register";
    public static final String TG_CHAT_EXISTS = TG_CHAT + "/check-exists";
    public static final String TG_CHAT_GET_ALL_REGISTER = TG_CHAT+"/getAll-chats";
    public static final String TG_LINK_SUBSCRIBE = TG_CHAT + "/subscribe-link";
    public static final String TG_GET_ALL_LINK = TG_LINK_SUBSCRIBE + "/get-subscribes";
    public static final String TG_LINK_SUBSCRIBE_EXISTS = TG_CHAT + "/subscribe-exists";
    public static final String TG_DELL_LINK = TG_CHAT + "/deleteSubsLink";
    public static final String TG_GET_COUNT_ANSWER = TG_CHAT + "/subscribe-link/count-answer";

    public static final String TG_STOCK = "/tg-stock";
    public static final String TG_STOCK_SUBSCRIBE = TG_STOCK + "/subscribe";
    public static final String TG_STOCK_CHECK_EXISTS = TG_STOCK + "/check-exists";
    public static final String TG_STOCK_UNSUBSCRIBE = TG_STOCK + "/unsubscribe";
    public static final String TG_STOCK_GET_SUBSCRIPTIONS = TG_STOCK + "/get-subscriptions";
}
