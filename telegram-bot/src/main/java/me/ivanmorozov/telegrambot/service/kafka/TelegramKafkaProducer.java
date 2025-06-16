package me.ivanmorozov.telegrambot.service.kafka;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendChatRegisterRequest(long chatId) {
        kafkaTemplate.send(
                KafkaTopics.REQUEST_TOPIC,
                String.valueOf(chatId),
                new KafkaRecords.KafkaRequest(chatId, MessageTypes.CHAT_REGISTER, Map.of())
        );
    }
}
