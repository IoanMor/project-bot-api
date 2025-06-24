package me.ivanmorozov.telegrambot.config;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.KafkaErrorHandler;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaDLQConfig {


    @Bean
    public KafkaErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaErrorHandler(kafkaTemplate);
    }

    @Bean
    public DefaultErrorHandler errorHandler (KafkaTemplate<String,Object> kafkaTemplate){
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000L,3));
    }

    @Bean
    @Primary
    public ProducerListener<String, Object> producerListener(KafkaErrorHandler kafkaErrorHandler) {
        return new ProducerListener<>() {
            @Override
            public void onError(ProducerRecord<String, Object> record, RecordMetadata metadata, Exception exception) {
                kafkaErrorHandler.handleProducerError(exception, record.value().toString());
            }

            @Override
            public void onSuccess(ProducerRecord<String, Object> record, RecordMetadata metadata) {
                log.info("✅ Сообщение отправлено: {}", record.value());
            }
        };
    }
}
