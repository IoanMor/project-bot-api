package me.ivanmorozov.scrapper.kafka;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.KafkaRecords;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScrapperKafkaProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void sendResponse(long chatId, KafkaRecords.KafkaResponse response) {
        kafkaTemplate.send(
                KafkaTopics.RESPONSE_TOPIC,
                String.valueOf(chatId),
                response
        );
    }

}
