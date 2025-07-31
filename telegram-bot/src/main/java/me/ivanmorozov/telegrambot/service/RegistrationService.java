package me.ivanmorozov.telegrambot.service;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j

public class RegistrationService {
    private final RegistrationCache cache;
    private final TelegramKafkaProducer kafkaProducer;
    private final TelegramSendMessage telegramSendMessage;

    public RegistrationService( RegistrationCache cache, TelegramKafkaProducer kafkaProducer, TelegramSendMessage telegramSendMessage) {
        this.cache = cache;
        this.kafkaProducer = kafkaProducer;
        this.telegramSendMessage = telegramSendMessage;
    }

    public boolean isChatRegister(long chatId,TelegramLongPollingBot bot) throws TelegramApiException {

        Boolean cachedStatus = cache.isRegistered(chatId);
        if (cachedStatus != null) {
            return cachedStatus;
        }

        kafkaProducer.sendIsChatRegisterRequest(chatId);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) {
            try {
                Thread.sleep(200);
                Boolean status = cache.isRegistered(chatId);
                if (status != null) {
                    return status;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TelegramApiException("Ожидание прервано");
            }
        }
        telegramSendMessage.sendMessage(bot,chatId, "⌛ Сервис временно недоступен, попробуйте позже");
        return false;
    }
}
