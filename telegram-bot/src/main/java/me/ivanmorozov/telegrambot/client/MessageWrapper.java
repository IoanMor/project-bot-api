package me.ivanmorozov.telegrambot.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageWrapper {
    private final MessageTelegramClient messageTelegramClient;

    public Mono<Void> sendMessage(long chatId, String text) {
        String formatted = formatMessage(text);
        return messageTelegramClient.sendMessageClient(chatId, formatted);
    }

    public Mono<Void> sendMessage(long chatId, String text, Object... args) {
        String formatted = formatMessage(text, args);
        return messageTelegramClient.sendMessageClient(chatId, formatted);
    }

    public Mono<Void> sendMessageWithDelay(long chatId, String text, Duration delay) {
        return Mono.delay(delay).then(sendMessage(chatId, text));
    }

    public Mono<Void> sendMessageWithDelay(long chatId, String text, Duration delay, Object... args) {
        return Mono.delay(delay).then(sendMessage(chatId, text, args));
    }

    private String formatMessage(String message) {
        if (message == null || message.isBlank()) {
            log.error("[.] В отправку сообщению пользователю пришло пустое сообщение! {}", "formatMessage");
            return "⚠️ Что то пошло не так";
        }
        return message.trim();
    }
    private String formatMessage(String message, Object... args) {
        if (message == null || message.isBlank()) {
            log.error("[.] В отправку сообщению пользователю пришло пустое сообщение! {}", "formatMessage");
            return "⚠️ Что то пошло не так";
        }
        return String.format(message,args).trim();
    }
}
