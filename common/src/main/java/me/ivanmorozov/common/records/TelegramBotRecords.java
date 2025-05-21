package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

public class TelegramBotRecords {
    public record TgMessageRequest(@Positive long chatId, String msg){}
}
