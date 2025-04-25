package me.ivanmorozov.telegrambot.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;

@Component
public class StackExchangeClient {
    private final WebClient webClient;
    public StackExchangeClient() {
        this.webClient = WebClient.builder()
                .baseUrl(STACK_API_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public  Mono<Boolean> trackLink(String link){
        return webClient.get()
                .uri(link)
                .retrieve()
                .bodyToMono(Boolean.class)
                .map(body -> true)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false);
    }

}
