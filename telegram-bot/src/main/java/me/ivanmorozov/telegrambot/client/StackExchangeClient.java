package me.ivanmorozov.telegrambot.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;

@Component
@Slf4j
public class StackExchangeClient {
    private final WebClient webClient;
    private final Map<Long, Integer> lastAnswerCounts = new ConcurrentHashMap<>();
    public StackExchangeClient() {
        this.webClient = WebClient.builder()
                .baseUrl(STACK_API_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public Mono<Boolean> trackLink(Long questionId) {
        String url = "/questions/" + questionId + "/answers?order=desc&sort=creation&site=stackoverflow&filter=total";
        log.info("Запрос к API: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.info("Ответ API: {}", response))
                .map(response -> {
                    int currentCount = response.path("total").asInt();
                    boolean hasNew = currentCount > lastAnswerCounts.getOrDefault(questionId, 0);
                    if (hasNew) {
                        lastAnswerCounts.put(questionId, currentCount);
                    }
                    return hasNew;
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Ошибка API: {}", e.getMessage());
                    return Mono.just(false);
                });
    }
}
