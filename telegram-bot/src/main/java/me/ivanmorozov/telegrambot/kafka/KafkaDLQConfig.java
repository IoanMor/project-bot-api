package me.ivanmorozov.telegrambot.kafka;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.KafkaErrorHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
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


   /* @Bean

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
    }*/

}
