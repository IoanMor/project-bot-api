package me.ivanmorozov.scrapper.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.services.db.ChatService;
import me.ivanmorozov.scrapper.services.db.LinkService;
import me.ivanmorozov.scrapper.services.db.StockService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
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
                    boolean isUnsubscribed = false;
                    if (linkService.isSubscribed(request.chatId(), link)) {
                        isUnsubscribed = linkService.unSubscribe(request.chatId(), link);
                    }
                    log.info("Пользователь {} отписался от {}", request.chatId(), link);
                    kafkaProducer.sendResponse(
                            request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.UNSUBSCRIBE_RESULT_LINK, Map.of(LINK_KEY, isUnsubscribed)
                            )
                    );
                }
            }

            case MessageTypes.LINK_GET_ALL_SUBS -> {
                Set<String> links = linkService.getAllSubscribeLinks(request.chatId());
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.GET_ALL_LINKS, Map.of(LINK_KEY, links)));
            }

            case MessageTypes.STOCK_SUBSCRIBE -> {
                if (request.data() instanceof Map<?, ?> dataMap && dataMap.get(STOCK_KEY) instanceof String ticker) {
                    boolean isSubscribed = stockService.isTickerSubscribed(request.chatId(), ticker);
                    if (isSubscribed) {
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_SUBSCRIBE_STOCK, Map.of(STOCK_KEY, ticker)
                                ));
                    } else {
                        boolean validateTickerNameResult = stockService.validateTickerName(ticker);
                        if (!validateTickerNameResult) {
                            kafkaProducer.sendResponse(request.chatId(),
                                    new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, false)
                                    ));
                        } else {
                            stockService.subscribe(request.chatId(), ticker);
                            kafkaProducer.sendResponse(request.chatId(),
                                    new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, ticker)
                                    ));
                        }
                    }
                }
            }
            case MessageTypes.STOCK_GET_ALL_SUBS -> {
                Set<String> subscribedStocks = stockService.getSubscriptions(request.chatId());
                Map<String, BigDecimal> stockPrices = new LinkedHashMap<>();
                for (var stock : subscribedStocks) {
                    stockPrices.put(stock, stockService.getStockPrice(stock));
                }
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.GET_ALL_STOCK, stockPrices));
            }
            case MessageTypes.STOCK_UNSUBSCRIBE -> {
                if (request.data() instanceof Map<?, ?> dataMap) {
                    boolean isUnsubscribe = false;
                    if (stockService.isTickerSubscribed(request.chatId(), (String) dataMap.get(STOCK_KEY))) {
                       isUnsubscribe = stockService.unsubscribe(request.chatId(), (String) dataMap.get(STOCK_KEY));
                    }
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.UNSUBSCRIBE_RESULT_STOCK, Map.of(STOCK_KEY, isUnsubscribe)));

                }
            }
        }
    }

}
