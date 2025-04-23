package me.ivanmorozov.common.endpoints;

public class ScrapperEndpoints {
    public static final String TG_CHAT = "/tg-chat";
    public static final String TG_CHAT_REGISTER = TG_CHAT + "/register";
    public static final String TG_CHAT_EXISTS = TG_CHAT + "/check-exists";

    private ScrapperEndpoints() {}
}
