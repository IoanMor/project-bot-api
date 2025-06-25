package me.ivanmorozov.scrapper.config;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
@EnableKafka
public class KafkaTopicConfig {


    @Bean
    public NewTopic requestTopic() {
        return buildTopic(KafkaTopics.REQUEST_TOPIC);
    }

    @Bean
    public NewTopic responseTopic() {
        return buildTopic(KafkaTopics.RESPONSE_TOPIC);
    }


    @Bean
    public NewTopic eventsTopic() {
        return buildTopic(KafkaTopics.EVENTS_TOPIC);
    }
    @Bean
    public NewTopic requestDlqTopic() {
        return buildTopic(KafkaTopics.DLQ_TOPIC_OUT);
    }

    @Bean
    public NewTopic responseDlqTopic() {
        return buildTopic(KafkaTopics.DLQ_TOPIC_IN);
    }

    private NewTopic buildTopic(String name) {
        log.info("Создание топика: {}", name);
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "compact")
                .build();
    }
}
