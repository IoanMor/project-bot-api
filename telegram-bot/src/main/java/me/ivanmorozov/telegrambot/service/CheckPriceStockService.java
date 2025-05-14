package me.ivanmorozov.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StockApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceStockService {
    private final ScrapperApiClient scrapperApiClient;
    private final StockApiClient stockApiClient;
    private final TelegramBotService telegramBotService;
    private final Scheduler scheduler = Schedulers.newParallel("checkUpdates", 4);
    @Scheduled(cron = "0 30 9 * * ?")
    public void sendTimeStock(){
        scrapperApiClient.getAllChats()
                .doOnNext(chatIds -> log.debug("Получены чаты для проверки: {}", chatIds))
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("Ошибка получения чатов: {}", e.getMessage()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .flatMapMany(Flux::fromIterable)
                .parallel()
                .runOn(scheduler)
                .flatMap(this::checkSubscribeStock)
                .subscribe(
                        null,
                        e -> log.error("Критическая ошибка рассылки: {}", e.getMessage())
                );
    }

    public Mono<Void> checkSubscribeStock(long chatId) {
        return scrapperApiClient.getSubscribeStock(chatId)
                .flatMapMany(Flux::fromIterable)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(ticker -> stockApiClient.getPrice(ticker)
                        .filter(price -> price.compareTo(BigDecimal.ZERO) >= 0)
                        .flatMap(price -> {
                            String msg = String.format("%s - цена = %.2f", ticker, price);
                            return telegramBotService.sendReactiveMsg(chatId, msg);
                        })
                        .onErrorResume(e -> {
                            log.error("Ошибка обработки акции {}: {}", ticker, e.getMessage());
                            return Mono.empty();
                        })
                )
                .then();
    }

}
