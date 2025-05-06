package me.ivanmorozov.common.records;

import jakarta.validation.constraints.Positive;

import java.util.Set;

public class ChatRecords {
    public record ChatExistsRequest(@Positive long id) {}
    public record ChatRegisterRequest(@Positive long id) {}
    public record ChatGetAllRegister(Set<Long> id){}
}
