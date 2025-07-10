package me.ivanmorozov.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import me.ivanmorozov.scrapper.config.WebClientConfig;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.time.Duration;

@Component
@Slf4j
public class StackOverflowClient {
    private final WebClient webClient;
    private final LinkRepository linkRepository;
    private final ScrapperMetrics scrapperMetrics;

    public StackOverflowClient(@Qualifier("stackOverFlowWebClient")WebClient webClient, LinkRepository linkRepository, ScrapperMetrics scrapperMetrics, WebClientConfig webClientConfig) {
        this.webClient = webClient;
        this.linkRepository = linkRepository;
        this.scrapperMetrics = scrapperMetrics;
    }

    public Mono<Boolean> trackLink(Long questionId, long chatId, String link) {
        String url = "/questions/" + questionId + "/answers?order=desc&sort=creation&site=stackoverflow&filter=total";
        log.info("Запрос к API: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.info("Ответ API: {}", response))
                .doOnSuccess(response -> scrapperMetrics.recordApiCallSuccess("stackoverflow-API"))
                .flatMap(response -> {
                    int currentCount = response.path("total").asInt();
                    return Mono.fromCallable(() -> linkRepository.getCountAnswer(chatId, link))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(storedCount -> {
                                if (currentCount > storedCount) {
                                    log.info("Новое сообщение по ссылке {} ", link);
                                    return Mono.fromRunnable(() ->
                                                    linkRepository.updateCountAnswer(chatId, link, currentCount))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .thenReturn(true);
                                } else {
                                    return Mono.just(false);
                                }
                            });


                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Ошибка API: {}", e.getMessage());
                    scrapperMetrics.recordApiCallFailure(String.valueOf(e));
                    return Mono.just(false);
                });
    }


}
