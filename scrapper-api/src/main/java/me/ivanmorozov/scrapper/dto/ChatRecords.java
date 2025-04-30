package me.ivanmorozov.scrapper.dto;

public class ChatRecords {
    public record ChatExistsRequest(long id) {}
    public record ChatRegisterRequest(long id) {}
}
