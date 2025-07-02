package me.ivanmorozov.scrapper.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.repositories.StockRepository;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperKafkaConsumer {
    private final TelegramChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final StockRepository stockRepository;
    private final ScrapperKafkaProducer kafkaProducer;
    private final StockApiClient stockApiClient;


    @KafkaListener(topics = KafkaTopics.REQUEST_TOPIC, groupId = "scrapper-api-group")
    public void handleRequest(KafkaRecords.KafkaRequest request) {
        log.info("[->] Получен запрос: {}", request);
        switch (request.type()) {
            case MessageTypes.CHAT_REGISTER -> {
                if (Objects.equals(chatRepository.existChat(request.chatId()),request.chatId())) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_REGISTER, Map.of()));
                } else {
                    chatRepository.registrationChat(request.chatId(), LocalDateTime.now());
                    log.info("SUCCESS/: Регистрация чата {}", request.chatId());
                    kafkaProducer.sendResponse(
                            request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.CREATED, Map.of())
                    );
                }
            }

            case MessageTypes.CHAT_CHECK -> {
                if (Objects.equals(chatRepository.existChat(request.chatId()),request.chatId())) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_CHECK, Map.of(EXIST_KEY,true)));
                } else {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT_CHECK, Map.of(EXIST_KEY,false)));
                }
            }

            case MessageTypes.LINK_SUBSCRIBE -> {
                Map<String, Object> data = request.data();
                String link = (String) data.get(LINK_KEY);
                if (linkRepository.existsLink(request.chatId(), link)) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_SUBSCRIBE_LINK, Map.of()));
                } else {
                    linkRepository.subscribeLink(request.chatId(), link);
                    log.info("SUCCESS/: {} Оформлена подписка на {}", request.chatId(), link);
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.SUCCESS, Map.of(LINK_KEY, link)));
                }
            }

            case MessageTypes.LINK_UNSUBSCRIBE -> {
                Map<String, Object> data = request.data();
                String link = (String) data.get(LINK_KEY);
                boolean isUnsubscribed = false;
                if (linkRepository.existsLink(request.chatId(), link)) {
                    linkRepository.removeLink(request.chatId(), link);
                    isUnsubscribed=true;
                }
                log.info("Пользователь {} отписался от {}", request.chatId(), link);
                kafkaProducer.sendResponse(
                        request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.UNSUBSCRIBE_RESULT_LINK, Map.of(LINK_KEY, isUnsubscribed))
                );
            }

            case MessageTypes.LINK_GET_ALL_SUBS -> {
                Set<String> links = linkRepository.getLinks(request.chatId());
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.GET_ALL_LINKS, Map.of(LINK_KEY, links)));
            }

            case MessageTypes.STOCK_SUBSCRIBE -> {
                Map<String, Object> data = request.data();
                String ticker = (String) data.get(STOCK_KEY);
                boolean isSubscribed = stockRepository.existsByTicker(request.chatId(), ticker);
                if (isSubscribed) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_SUBSCRIBE_STOCK, Map.of(STOCK_KEY, ticker)));
                } else {
                    boolean isValid = Boolean.TRUE.equals(stockApiClient.validateTickerName(ticker).block());
                    if (!isValid) {
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, false)));
                    } else {
                        stockRepository.subscribeStock(request.chatId(), ticker);
                        kafkaProducer.sendResponse(request.chatId(),
                                new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.ACCEPTED, Map.of(STOCK_KEY, ticker)));
                    }
                }
            }

            case MessageTypes.STOCK_GET_ALL_SUBS -> {
                Set<String> subscribedStocks = stockRepository.getTickers(request.chatId());
                Map<String, BigDecimal> stockPrices = new LinkedHashMap<>();
                for (String stock : subscribedStocks) {
                    stockPrices.put(stock, stockApiClient.getPrice(stock).block());
                }
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.GET_ALL_STOCK, stockPrices));
            }

            case MessageTypes.STOCK_UNSUBSCRIBE -> {
                Map<String, Object> data = request.data();
                String ticker = (String) data.get(STOCK_KEY);
                boolean isUnsubscribe = false;
                if (stockRepository.existsByTicker(request.chatId(), ticker)) {
                     stockRepository.removeStock(request.chatId(), ticker);
                     isUnsubscribe = true;
                }
                kafkaProducer.sendResponse(request.chatId(),
                        new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.UNSUBSCRIBE_RESULT_STOCK, Map.of(STOCK_KEY, isUnsubscribe)));
            }
        }
    }
}
