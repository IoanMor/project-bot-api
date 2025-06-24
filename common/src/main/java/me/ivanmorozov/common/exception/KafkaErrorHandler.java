package me.ivanmorozov.telegrambot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
public class KafkaErrorHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void handleProducerError(Throwable ex, String failedRequest) {
        log.error("❌ ERROR - ошибка ОТПРАВКИ запроса через Kafka", ex);
        kafkaTemplate.send("bot.dlq.out", failedRequest)
                .handle((result, ex2) -> {
                    if (ex2 != null) {
                        log.error("❌ Не удалось отправить сообщение в DLQ bot.dlq.out", ex2);
                    } else {
                        log.info("✅ Сообщение отправлено в DLQ bot.dlq.out");
                    }
                    return null;
                });
    }

    public void handleConsumerError(Throwable ex, String failedResponse) {
        log.error("❌ ERROR - ошибка ПОЛУЧЕНИЯ ответа через Kafka", ex);
        kafkaTemplate.send("bot.dlq.in", failedResponse)
                .handle((result, ex2) -> {
                    if (ex2 != null) {
                        log.error("❌ Не удалось отправить сообщение в DLQ bot.dlq.in", ex2);
                    } else {
                        log.info("✅ Сообщение отправлено в DLQ bot.dlq.in");
                    }
                    return null;
                });
    }
}
