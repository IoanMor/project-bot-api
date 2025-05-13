package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

public class StockRecords {
    public record StockSubscribeRequest(@Positive long chatId, String ticker){}
    public record StockExistRequest(@Positive long chatId, String ticker){}
    public record StockGetRequest(@Positive long chatId){}
}
