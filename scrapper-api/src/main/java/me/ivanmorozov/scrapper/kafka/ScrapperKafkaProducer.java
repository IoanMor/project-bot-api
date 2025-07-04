package me.ivanmorozov.scrapper.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperKafkaProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final ScrapperMetrics metrics;

    public void sendResponse(long chatId, KafkaRecords.KafkaResponse response) {
        kafkaTemplate.send(
                KafkaTopics.RESPONSE_TOPIC,
                String.valueOf(chatId),
                response
        ).handle((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки в Kafka", ex);
            } else {
                metrics.recordKafkaMessageCountResponse(response.type());
                log.info("Отправлено в Kafka: {}", response);
            }
            return null;
        });
    }
}
