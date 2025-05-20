package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

import java.util.Set;

public class ChatRecords {
    public record ChatExistsRequest(@Positive long chatId) {}
    public record ChatRegisterRequest(@Positive long chatId) {}

}
