package me.ivanmorozov.common.records;

import me.ivanmorozov.common.kafka.MessageTypes;

import java.util.Map;

public class KafkaRecords {
    public record KafkaRequest (long chatId, String type, Map<String,Object> data){}
    public record KafkaResponse (long chatId, String type, Object data){}
}
