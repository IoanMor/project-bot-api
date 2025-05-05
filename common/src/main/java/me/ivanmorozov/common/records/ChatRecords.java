package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

public class ChatRecords {
    public record ChatExistsRequest(@Positive long id) {}
    public record ChatRegisterRequest(@Positive long id) {}
}
