package me.ivanmorozov.telegrambot.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void send(String topic, String key, T obj) {
        kafkaTemplate.send(topic, key, obj);
    }
}
