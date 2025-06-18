package me.ivanmorozov.scrapper.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.services.db.ChatService;
import me.ivanmorozov.scrapper.services.db.LinkService;
import me.ivanmorozov.scrapper.services.db.StockService;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperKafkaConsumer {
    private final ChatService chatService;
    private final LinkService linkService;
    private final StockService stockService;
    private final ScrapperKafkaProducer kafkaProducer;


    @KafkaListener(topics = KafkaTopics.REQUEST_TOPIC, groupId = "scrapper-api-group")
    public void handleRequest(KafkaRecords.KafkaRequest request) {
        switch (request.type()) {
            case MessageTypes.CHAT_REGISTER -> {

                if (chatService.isChatExist(request.chatId())) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_REGISTER, Map.of()));
                } else {
                    chatService.registerChat(request.chatId());
                    log.info("SUCCESS/: Регистрация чата {}", request.chatId());
                    kafkaProducer.sendResponse(
                            request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.CREATED, Map.of())
                    );
                }
            }

            case MessageTypes.CHAT_CHECK -> {
                if (chatService.isChatExist(request.chatId())) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_CHECK, Map.of(EXIST_KEY, true)));
                } else {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_CHECK, Map.of(EXIST_KEY, false)));
                }
            }
            case MessageTypes.LINK_SUBSCRIBE -> {

                if (request.data() instanceof Map<?, ?> dataMap && dataMap.get(LINK_KEY) instanceof String link) {
                    link = (String) dataMap.get(LINK_KEY);
                    if (linkService.isSubscribed(request.chatId(), link)) {
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_SUBSCRIBE_LINK, Map.of()));
                    } else {
                        linkService.subscribe(request.chatId(), link);
                        log.info("SUCCESS/: {} Оформлена подписка на {}", request.chatId(), link);
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.SUCCESS, Map.of(LINK_KEY, link)));
                    }
                }
            }
            case MessageTypes.LINK_UNSUBSCRIBE -> {
                if (request.data() instanceof Map<?, ?> dataMap && dataMap.get(LINK_KEY) instanceof String link) {
                    boolean isUnsubscribed = linkService.unSubscribe(request.chatId(), link);
                    kafkaProducer.sendResponse(
                            request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.OPERATION_RESULT, Map.of(LINK_KEY, isUnsubscribed)
                            )
                    );
                }
            }

            case MessageTypes.LINK_GET_ALL_SUBS -> {
                Set<String> links = linkService.getAllSubscribeLinks(request.chatId());
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.GET_ALL_LINKS,Map.of(LINK_KEY,links)));
            }
            case MessageTypes.STOCK_SUBSCRIBE -> {
                if (request.data() instanceof Map<?,?> dataMap && dataMap.get(STOCK_KEY) instanceof String ticker){
                    if (!stockService.isTickerSubscribed(request.chatId(), ticker)) {
                        stockService.subscribe(request.chatId(), ticker);
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, true)));
                    } else {
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, false)));
                    }
                }
            }
        }
    }

}
