package me.ivanmorozov.telegrambot.core;

import lombok.RequiredArgsConstructor;

import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CommandDispatcher {
    private final List<BotCommandHandler> commandHandlers;
    private final MessageWrapper messageWrapper;


    public void dispatch(String message, long chatId, String userName) {
        if (message == null || !message.startsWith("/")) return;
        String[] parts = message.trim().split("\\s+");
        String command = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        for (BotCommandHandler handler : commandHandlers) {
            if (Objects.equals(handler.getCommand(), command)) {
                handler.execute(chatId, userName, args);
                return;
            }
        }
        messageWrapper.sendMessage(chatId, "❓ Неизвестная команда: " + command + "\nВведите /help для списка доступных команд")
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }
    }


