package me.ivanmorozov.telegrambot.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageWrapper {
    private final MessageTelegramClient messageTelegramClient;

    public void sendMessage(long chatId, String text) {
        String formatted = formatMessage(text);
        messageTelegramClient.sendMessageClient(chatId, formatted).subscribe();
    }

    private String formatMessage(String message) {
        if (message == null || message.isBlank()) {
            log.error("[.] В отправку сообщению пользователю пришло пустое сообщение! {}", "formatMessage");
            return "⚠️ Что то пошло не так";
        }
        return message.trim();
    }
}
