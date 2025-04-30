package me.ivanmorozov.scrapper.dto;

public class LinkRecords {
    public record LinkExistRequest(long chatId, String link){}
    public record LinkSubscribeRequest(long chatId, String link){}
    public record LinkGetRequest(long chatId){}
}
