package me.ivanmorozov.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StackExchangeClient;

import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckSubscribeService {
    private final ScrapperApiClient scrapperApiClient;
    private final StackExchangeClient stackClient;
    private final TelegramBotService tgService;
    private final Scheduler scheduler = Schedulers.boundedElastic();

    @Scheduled(fixedDelay = 300_000) // 5 мин
    public void checkUpdates() {
        scrapperApiClient.getAllChats()
                .timeout(Duration.ofSeconds(10))
                .flatMapMany(Flux::fromIterable)
                .flatMap(chatId -> checkUserSubscriptions(chatId)
                        .subscribeOn(scheduler)
                        .onErrorResume(e -> {
                    log.error("Ошибка при проверке подписок chatId={}: {}", chatId, e.getMessage());
                    return Mono.empty();
                }))
                .subscribe();
    }

    private Mono<Void> checkUserSubscriptions(Long chatId) {
        return scrapperApiClient.getAllLinks(chatId)
                .timeout(Duration.ofSeconds(10))
                .flatMapMany(Flux::fromIterable)
                .flatMap(link -> {
                    Optional<Long> questIdOpt  = tgService.parseQuestionId(link);
                    if (questIdOpt.isEmpty()){
                        log.warn("Невозможно распарсить ссылку: {}", link);
                        return Mono.empty();
                    }
                    return stackClient.trackLink(questIdOpt)
                            .filter(hasUpdates -> hasUpdates)
                            .flatMap(ifUpd -> tgService.sendReactiveMsg(chatId, "🔔 Новый ответ на вопрос: " + link));
                        })

                .then();
    }



}
