package me.ivanmorozov.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StackExchangeClient;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckSubscribeService {
    private final ScrapperApiClient scrapperApiClient;
    private final StackExchangeClient stackClient;
    private final TelegramBotService tgService;

    @Scheduled(fixedDelay = 60000) // 1 мин
    public void checkUpdates() {
        log.info("=== Начало проверки обновлений ===");
        scrapperApiClient.getAllChats()
                .doOnNext(chatIds -> log.debug("Получены чаты для проверки: {}", chatIds))
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("Ошибка получения чатов: {}", e.getMessage()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .flatMapMany(Flux::fromIterable)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::checkUserSubscriptions)
                .subscribe(null,
                        e -> log.error("Ошибка в checkUpdates: {}", e.getMessage()),
                        () -> log.info("=== Проверка обновлений завершена ==="));
    }

    public Mono<Void> checkUserSubscriptions(Long chatId) {
        return scrapperApiClient.getAllLinks(chatId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(link -> {
                    try {
                        Long questionId = tgService.parseQuestionId(link)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid link"));

                        return stackClient.trackLink(questionId)
                                .filter(Boolean.TRUE::equals)
                                .flatMap(__ -> {
                                    String msg = "🔔 Новый ответ: " + link;
                                    log.info("Отправка: chatId={}, msg={}", chatId, msg);
                                    return tgService.sendReactiveMsg(chatId, msg);
                                });
                    } catch (Exception e) {
                        log.error("Ошибка обработки ссылки: {}", link, e);
                        return Mono.empty();
                    }
                })
                .then();
    }


}
