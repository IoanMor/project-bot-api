package me.ivanmorozov.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.services.db.LinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;

@Component
@Slf4j

public class StackOverflowClient {
    private final WebClient webClient;
    private final LinkService linkService;


    public StackOverflowClient(LinkService linkService) {
        this.linkService = linkService;

        this.webClient = WebClient.builder()
                .baseUrl(STACK_API_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public Mono<Boolean> trackLink(Long questionId, long chatId, String link) {
        String url = "/questions/" + questionId + "/answers?order=desc&sort=creation&site=stackoverflow&filter=total";
        log.info("Запрос к API: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.info("Ответ API: {}", response))
                .map(response -> {
                    int currentCount = response.path("total").asInt();
                    int storedCount = linkService.getCountAnswer(chatId, link);
                    if (currentCount > storedCount) {
                        log.info("Новое сообщение по ссылке {} ",link);
                        linkService.updateCountAnswer(chatId, link, currentCount);
                        return true;
                    }
                    return false;
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Ошибка API: {}", e.getMessage());
                    return Mono.just(false);
                });
    }



}
