package me.ivanmorozov.telegrambot.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;

@Component
public class StackExchangeClient {
    private final WebClient webClient;
    private final Map<Long, Integer> lastAnswerCounts = new ConcurrentHashMap<>();
    public StackExchangeClient() {
        this.webClient = WebClient.builder()
                .baseUrl(STACK_API_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public  Mono<Boolean> trackLink(long questionID){
        return webClient.get()
                .uri("/questions/{id}/answers?order=desc&sort=creation&site=stackoverflow&filter=total", questionID)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    Integer currentCount = response.path("total").asInt();
                    Integer previousCount = lastAnswerCounts.get(questionID);
                    lastAnswerCounts.put(questionID,currentCount);
                   return previousCount == null || currentCount > previousCount;
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false);
    }

}
