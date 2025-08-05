package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StackOverflowClient;

import me.ivanmorozov.scrapper.repositories.LinkRepository;

import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.STOCK_KEY;
import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.parseQuestionId;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckSubscribeService {
    private final LinkRepository linkRepository;
    private final StackOverflowClient client;
    private final ScrapperKafkaProducer kafkaProducer;
    private final ReactiveMethodsDB reactiveMethod;

    @Scheduled(fixedDelay = 60000) // 1 мин
    public void checkUpdates() {
        log.info("=== Запуск проверки обновлений ===");
        reactiveMethod.getAllChatsWithRetry()
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(chatId -> checkUserSubscriptions(chatId)
                        .timeout(Duration.ofSeconds(30))
                        .onErrorResume(e -> {
                            log.error("Ошибка при проверке чата {}: {}", chatId, e.getMessage());
                            return Mono.empty();
                        }))
                .sequential()
                .subscribe(
                        result -> {
                        },
                        error -> log.error("Фатальная ошибка в потоке проверки: {}", error.getMessage()),
                        () -> log.info("Цикл проверки завершен успешно")
                );
    }

    public Mono<Void> checkUserSubscriptions(Long chatId) {
        return Mono.fromCallable(() -> linkRepository.getLinks(chatId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(link -> {
                    Long questionId = parseQuestionId(link)
                            .orElseThrow(() -> new IllegalArgumentException("Некорректная ссылка"));

                    return client.shouldNotifyForLink(questionId, chatId, link)
                            .timeout(Duration.ofSeconds(5))
                            .onErrorResume(e -> {
                                log.error("Ошибка при проверке кол-ва ответов {}: {}", link, e.getMessage());
                                return Mono.empty();
                            })
                            .filter(Boolean.TRUE::equals)
                            .flatMap(__ -> {
                                String msg = "🔔 Новый ответ: " + link;
                                log.info("Отправка: chatId={}, msg={}", chatId, msg);
                                return Mono.fromRunnable(() ->
                                        kafkaProducer.sendResponse(chatId,
                                                new KafkaRecords.KafkaResponse(chatId, MessageTypes.STOCK_SHEDULED_MSG, Map.of(STOCK_KEY, msg)))
                                ).then();

                            }).onErrorResume(e -> {
                                log.error("Ошибка обработки ссылки: {}", link, e);
                                return Mono.empty();
                            });

                })
                .then();
    }


}
