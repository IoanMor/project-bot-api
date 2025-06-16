package me.ivanmorozov.common.records;

import me.ivanmorozov.common.kafka.MessageTypes;

public class KafkaRecords {
    public record KafkaRequest (long chatId, String type, Object data){}
    public record KafkaResponse (long chatId, String type, Object data){}
}
