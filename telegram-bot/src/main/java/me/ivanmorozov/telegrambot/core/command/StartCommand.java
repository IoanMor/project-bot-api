package me.ivanmorozov.telegrambot.core.command;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;

import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageWrapper messageWrapper;
    private final MeterRegistry meterRegistry;
    private Counter startCommandCounter;

    @PostConstruct
    public void initCounter() {
        startCommandCounter = meterRegistry.counter("telegram.bot.command.start");
    }
    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(long chatId, String userName,String[] args) {
        try {
            String safeName = userName != null ? userName : "пользователь";
            messageWrapper.sendMessage(chatId, "Приветствую, %s.",safeName).subscribe();
            kafkaProducer.sendChatRegisterRequest(chatId);
            messageWrapper.sendMessage(chatId, "🔍 Проверяем вашу регистрацию...").subscribe();
        } catch (Exception e) {
            log.error("Ошибка при выполнении /start для chatId={}", chatId, e);
            messageWrapper.sendMessage(chatId, "⚠️ Временная ошибка сервера").subscribe();
            throw new RuntimeException("Ошибка при выполнении /start", e);
        }
    }
}
