package me.ivanmorozov.telegrambot.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.MessageTelegramClient;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;

import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageTelegramClient sendMessage;
    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(long chatId, String userName,String[] args) {
        try {
            String safeName = userName != null ? userName : "пользователь";
            sendMessage.sendMessageClient(chatId, "Приветствую, " + safeName + "...").subscribe();
            kafkaProducer.sendChatRegisterRequest(chatId);
            sendMessage.sendMessageClient(chatId, "🔍 Проверяем вашу регистрацию...").subscribe();
        } catch (Exception e) {
            sendMessage.sendMessageClient(chatId, "⚠️ Временная ошибка сервера").subscribe();
            throw new RuntimeException("Ошибка при выполнении /start", e);
        }
    }
}
