package me.ivanmorozov.common.kafka;

public class KafkaTopics {
    public static final String REQUEST_TOPIC = "scrapper.requests";
    public static final String RESPONSE_TOPIC = "scrapper.responses";
    public static final String EVENTS_TOPIC = "events";
    public static final String DLQ_TOPIC_IN ="bot.dlq.in";
    public static final String DLQ_TOPIC_OUT = "bot.dlq.out";
}
