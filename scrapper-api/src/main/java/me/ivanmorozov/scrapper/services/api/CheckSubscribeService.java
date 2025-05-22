package me.ivanmorozov.scrapper.services.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.ivanmorozov.scrapper.client.StackOverflowClient;

import me.ivanmorozov.scrapper.client.TelegramBotClient;
import me.ivanmorozov.scrapper.services.db.ChatService;
import me.ivanmorozov.scrapper.services.db.LinkService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.parseQuestionId;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckSubscribeService {
    private final LinkService linkService;
    private final ChatService chatService;
    private final TelegramBotClient botClient;
    private final StackOverflowClient client;

    @Scheduled(fixedDelay = 60000) // 1 мин
    public void checkUpdates() {
      chatService.getAllChatsWithRetry()
              .flatMap(this::checkUserSubscriptions).subscribeOn(Schedulers.boundedElastic())
              .doOnComplete(()-> log.info("=Проверка чатов завершена="))
              .subscribe(
                      null,
                      error -> log.error("Фатальная ошибка при проверке: {}", error.getMessage())
              );
    }
    public Mono<Void> checkUserSubscriptions(Long chatId) {
        return Flux.fromIterable(linkService.getAllSubscribeLinks(chatId))
                .flatMap(link -> {
                    try {
                        Long questionId = parseQuestionId(link)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid link"));

                        return client.trackLink(questionId, chatId, link)
                                .filter(Boolean.TRUE::equals)
                                .flatMap(__ -> {
                                    String msg = "🔔 Новый ответ: " + link;
                                    log.info("Отправка: chatId={}, msg={}", chatId, msg);
                                    return botClient.sendReactiveMsg(chatId, msg).timeout(Duration.ofSeconds(5));
                                });
                    } catch (Exception e) {
                        log.error("Ошибка обработки ссылки: {}", link, e);
                        return Mono.empty();
                    }
                })
                .then();
    }

}
