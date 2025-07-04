package me.ivanmorozov.telegrambot.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;

import me.ivanmorozov.telegrambot.metric.BotMetrics;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import java.util.Map;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.LINK_KEY;
import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.STOCK_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramKafkaProducer {
    private final KafkaTemplate<String, KafkaRecords.KafkaRequest> kafkaTemplate;
    private final BotMetrics metrics;


    private void sendRequestToKafka(long chatId, KafkaRecords.KafkaRequest request) {
        kafkaTemplate.send(KafkaTopics.REQUEST_TOPIC, String.valueOf(chatId), request)
                .handle((result, ex2) -> {
                    if (ex2 != null) {
                        log.error("[.] Ошибка при отпрваке запроса exception-{}" + "\n" + "request-{}", ex2, request.toString());
                    } else {
                        metrics.recordKafkaMessageCountRequest(request.type());
                        log.info("[->] Отправлено в Kafka: {}", request);
                    }
                    return null;
                });

    }

    public void sendChatRegisterRequest(long chatId) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.CHAT_REGISTER, Map.of()));
    }

    public void sendIsChatRegisterRequest(long chatId) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.CHAT_CHECK, Map.of()));
    }

    public void sendSubscribeLinkRequest(long chatId, String link) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.LINK_SUBSCRIBE, Map.of(LINK_KEY, link)));
    }

    public void sendUnSubscribeLinkRequest(long chatId, String link) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.LINK_UNSUBSCRIBE, Map.of(LINK_KEY, link)));
    }

    public void sendAllSubscribeLinksRequest(long chatId) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.LINK_GET_ALL_SUBS, Map.of()));
    }

    public void sendSubscribeStockRequest(long chatId, String ticker) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.STOCK_SUBSCRIBE, Map.of(STOCK_KEY, ticker)));
    }

    public void sendGetStockSubscribeRequest(long chatId) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.STOCK_GET_ALL_SUBS, Map.of()));
    }

    public void sendUnSubscribeStockRequest(long chatId, String ticker) {
        sendRequestToKafka(chatId, new KafkaRecords.KafkaRequest(chatId, MessageTypes.STOCK_UNSUBSCRIBE, Map.of(STOCK_KEY, ticker)));
    }

}
