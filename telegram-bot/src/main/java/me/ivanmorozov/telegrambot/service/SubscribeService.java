package me.ivanmorozov.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StackExchangeClient;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {
    private final ScrapperApiClient scrapperApiClient;
    private final StackExchangeClient stackClient;
    private final TelegramBotService tgService;

    @Scheduled(fixedDelay = 300_000) // 5 мин
    public void checkUpdates() {
        scrapperApiClient.getAllChats()
                .timeout(Duration.ofSeconds(10))
                .flatMap(chatId -> checkUserSubscriptions(chatId).onErrorResume(e -> {
                    log.error("Ошибка при проверке подписок chatId={}: {}", chatId, e.getMessage());
                    return Mono.empty();
                }))


    }
    private Mono<Void> checkUserSubscriptions(Long chatId) {
        return scrapperApiClient.getLinks(chatId)
                .timeout(Duration.ofSeconds(10))
                .flatMap(link -> checkLinkUpdates(chatId, link))
                .then();
    }

}
