package me.ivanmorozov.telegrambot.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;

@Component
@Slf4j

public class StackExchangeClient {
    private final WebClient webClient;
    private final ScrapperApiClient client;


    public StackExchangeClient(ScrapperApiClient client) {
        this.client = client;
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
                    int storedCount = client.getCountAnswer();

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
