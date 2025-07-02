package me.ivanmorozov.telegrambot.core;

import org.springframework.stereotype.Service;

@Service
public interface BotCommandHandler {
    public String getCommand();

    public void execute(long chatId, String userName, String[] args);
}
