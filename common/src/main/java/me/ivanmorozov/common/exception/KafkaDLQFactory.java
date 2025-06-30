package me.ivanmorozov.common.exception;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

public class KafkaDLQFactory {


    public static DefaultErrorHandler createErrorHandler(KafkaTemplate<String, Object> kafkaTemplate){
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, e) -> new TopicPartition("bot.dlq.in", -1));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            System.err.printf("Ошибка при обработке сообщения. Попытка #%d. Ошибка: %s%n",
                    deliveryAttempt, ex.getMessage());
            ex.printStackTrace();
        });
        return errorHandler;
    }
}
