package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

import java.time.Instant;


public class ChatRecords {
    public record ChatExistsRequest(@Positive long chatId) {}
    public record ChatExistsResponse(@Positive long chatId, boolean isExist) {}
    public record ChatRegisterRequest(@Positive long chatId) {}

}
