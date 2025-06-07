package me.ivanmorozov.scrapper.config;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaTopicConfig {
    @Bean
    public NewTopic tgChatCreateTopic() {
        return createTopic(KafkaTopics.TG_CHAT_CREATE);
    }
    @Bean
    public NewTopic tgChatCreatedTopic() {
        return createTopic(KafkaTopics.TG_CHAT_CREATED);
    }

    @Bean
    public NewTopic tgChatExistRequestTopic() {
        return createTopic(KafkaTopics.TG_CHAT_EXIST_REQ);
    }

    @Bean
    public NewTopic tgChatExistResponseTopic() {
        return createTopic(KafkaTopics.TG_CHAT_EXIST_RES);
    }


    @Bean
    public NewTopic linkSubscribeTopic() {
        return createTopic(KafkaTopics.LINK_SUBSCRIBE);
    }

    @Bean
    public NewTopic linkExistRequestTopic() {
        return createTopic(KafkaTopics.LINK_SUBSCRIBE_EXIST_REQ);
    }
    @Bean
    public NewTopic linkExistResponseTopic() {
        return createTopic(KafkaTopics.LINK_SUBSCRIBE_EXIST_RES);
    }

    @Bean
    public NewTopic stockTopic() {
        return createTopic(KafkaTopics.STOCK);
    }


    private NewTopic createTopic(String name) {
        log.info("Creating topic: {}", name);
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(2)
                .config(TopicConfig.RETENTION_MS_CONFIG, "259200000")// 3дня
                .build();
    }
}
