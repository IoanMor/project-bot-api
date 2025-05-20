package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;


public class LinkRecords {
    public record LinkExistRequest(@Positive long chatId, String link){}
    public record LinkSubscribeRequest(@Positive long chatId, String link){}
    public record LinkGetRequest(@Positive long chatId){}
    public record LinkGetCountAnswerRequest(long chatId, String link){}
}
