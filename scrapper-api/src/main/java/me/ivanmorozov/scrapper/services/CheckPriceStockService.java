package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StockApiClient;


import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;

import me.ivanmorozov.scrapper.repositories.StockRepository;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.STOCK_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceStockService {
    private final StockRepository stockRepository;
    private final StockApiClient stockApiClient;
    private final ScrapperKafkaProducer kafkaProducer;
    private final ReactiveMethodsDB reactiveMethod;

    @Scheduled(cron = "0 30 9 * * ?") // в 9 30 утра
    //@Scheduled(cron = "*/30 * * * * ?") // 30 сек
    public void sendTimeStock() {
        reactiveMethod.getAllChatsWithRetry()
                .flatMap(this::checkSubscribeStock)
                .doOnComplete(() -> log.info("=Проверка чатов завершена="))
                .subscribe(
                        null,
                        error -> log.error("Фатальная ошибка при проверке: {}", error.getMessage())
                );
    }


    public Mono<Void> checkSubscribeStock(long chatId) {
        return Mono.fromCallable(() -> stockRepository.getTickers(chatId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(ticker ->
                        stockApiClient.getPrice(ticker)
                                .filter(price -> price.compareTo(BigDecimal.ZERO) >= 0)
                                .map(price -> Map.entry(ticker, price))
                                .onErrorResume((e)->{
                                    log.error("Ошибка при проверке цены акции {},{}",chatId,e.getMessage());
                                    return Mono.empty();
                                })

                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .flatMap(stockPrices -> {
                    if (stockPrices.isEmpty()) {
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
                    log.info("Отправка: chatId={}, msg={}", chatId, message);
                    return Mono.fromRunnable(() ->
                            kafkaProducer.sendResponse(chatId,
                                    new KafkaRecords.KafkaResponse(chatId, MessageTypes.STOCK_SHEDULED_MSG, Map.of(STOCK_KEY, message.toString())))
                    ).then();
                })
             .subscribeOn(Schedulers.boundedElastic());

    }


}


