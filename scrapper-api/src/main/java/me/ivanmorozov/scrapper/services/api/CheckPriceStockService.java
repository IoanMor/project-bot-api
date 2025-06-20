package me.ivanmorozov.scrapper.services.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.StockServiceException;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.client.TelegramBotClient;
import me.ivanmorozov.scrapper.services.db.ChatService;
import me.ivanmorozov.scrapper.services.db.StockService;
import me.ivanmorozov.scrapper.services.kafka.ScrapperKafkaProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.STOCK_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceStockService {
    private final ChatService chatService;
    private final StockService stockService;
    private final StockApiClient stockApiClient;

    private final ScrapperKafkaProducer kafkaProducer;

    // @Scheduled(cron = "0 30 9 * * ?") // в 9 30 утра
    @Scheduled(cron = "*/30 * * * * ?") // 30 сек
    public void sendTimeStock() {
        chatService.getAllChatsWithRetry()
                .flatMap(this::checkSubscribeStock).subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> log.info("=Проверка чатов завершена="))
                .subscribe(
                        null,
                        error -> log.error("Фатальная ошибка при проверке: {}", error.getMessage())
                );
    }

    public Mono<Void> checkSubscribeStock(long chatId) {
        return Flux.fromIterable(stockService.getSubscriptions(chatId))
                .flatMap(ticker ->
                    stockApiClient.getPrice(ticker)
                            .filter(price -> price.compareTo(BigDecimal.ZERO) >= 0)
                            .map(price -> Map.entry(ticker,price))
                )
                .collectMap(Map.Entry::getKey,Map.Entry::getValue)
                .flatMap(stockPrices->{
                    if (stockPrices.isEmpty()){
                        log.info("Нет данных по акциям для chatId: {}", chatId);
                        return Mono.empty();
                    }
                    StringBuilder message = new StringBuilder("📋 Стоимость акций:\n");
                    int counter = 1;
                    for (Map.Entry<String, BigDecimal> entry : stockPrices.entrySet()) {
                        message.append(counter++)
                                .append(" - ")
                                .append(entry.getKey())
                                .append(" - rub.")
                                .append(String.format("%.2f", entry.getValue()))
                                .append("\n");
                    }
                    return Mono.fromRunnable(() ->
                            kafkaProducer.sendResponse(chatId,
                                    new KafkaRecords.KafkaResponse(chatId, MessageTypes.STOCK_SHEDULED_MSG, Map.of(STOCK_KEY, message.toString())))
                    ).then();
                }).subscribeOn(Schedulers.boundedElastic());

    }


}
