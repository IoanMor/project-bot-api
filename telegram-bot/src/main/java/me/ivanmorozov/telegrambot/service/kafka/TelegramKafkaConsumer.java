package me.ivanmorozov.telegrambot.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.ChatRecords;

import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.common.records.LinkRecords;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static me.ivanmorozov.common.constMsg.Msg.START_TEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramKafkaConsumer {
    private final TelegramBotService botService;
    private final TelegramKafkaProducer kafkaProducer;

    @KafkaListener(topics = KafkaTopics.RESPONSE_TOPIC)
    public void handleResponse(KafkaRecords.KafkaResponse response){
        switch (response.type()){
            case MessageTypes.CREATED -> {
                botService.sendMsg(response.chatId(), "✅ Регистрация завершена! " + START_TEXT);
            }
            case MessageTypes.NOT_CREATED -> {
                botService.sendMsg(response.chatId(), "⚠️ Не удалось зарегистрировать, попробуйте позже");
            }
            case MessageTypes.EXIST_CHAT -> {
                botService.sendMsg(response.chatId(), "ℹ️ Вы уже зарегистрированы.");
            }
            default -> {
                log.warn("Неизвестный тип: {}", response.type());
                botService.sendMsg(response.chatId(), "⚠️ Произошла непредвиденная ошибка");
            }
        }
    }

}
