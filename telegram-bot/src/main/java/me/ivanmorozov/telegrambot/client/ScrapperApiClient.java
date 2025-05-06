package me.ivanmorozov.telegrambot.client;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.LinkRecords;
import me.ivanmorozov.common.exception.LinkSubscribeException;

import me.ivanmorozov.common.endpoints.ScrapperEndpoints;
import me.ivanmorozov.common.exception.ChatAlreadyExistsException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Set;

import static me.ivanmorozov.common.endpoints.ScrapperEndpoints.*;

@Component
@Slf4j
public class ScrapperApiClient {
    private final WebClient webClient;
    private final Duration TIMEOUT = Duration.ofSeconds(5);

    public ScrapperApiClient() {
        this.webClient = WebClient.create(TG_CHAT_URI);
    }

    public Mono<Boolean> registerChat(long chatId) {
        return webClient.post()
                .uri(ScrapperEndpoints.TG_CHAT_REGISTER)
                .bodyValue(new ChatRecords.ChatRegisterRequest(chatId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("Failed to register chat. Status: " + response.statusCode() +
                                    ", Body: " + body)));
                })
                .toBodilessEntity()
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .thenReturn(true)
                .onErrorResume(e -> {
                    log.error("Ошибка регистрации чата {}: {}", chatId, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Set<Long>> getAllChats() {
        return webClient.post()
                .uri(TG_CHAT_GET_ALL_REGISTER)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<Long>>(){});
    }

    public Mono<Boolean> isChatRegister(long chatId) {
        return webClient.post()
                .uri(TG_CHAT_EXISTS)
                .bodyValue(new ChatRecords.ChatExistsRequest(chatId))
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals,
                        response -> Mono.error(new ChatAlreadyExistsException(chatId)))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Ошибка поиска чата {}:{}", chatId, e.getMessage());
                    return Mono.just(false);

                });
    }

    public Mono<Boolean> subscribeLink(long chatId, String link) {
        return webClient.post()
                .uri(TG_CHAT_LINK_SUBSCRIBE)
                .bodyValue(new LinkRecords.LinkSubscribeRequest(chatId, link))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new LinkSubscribeException("Ошибка подписки на ссылку - " + link)))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .doOnError(e -> log.error(
                        "Ошибка подписки. Чат: {}, Ссылка: {}, Причина: {}",
                        chatId, link, e.getMessage()
                ))
                .onErrorReturn(false);
    }

    public Mono<Boolean> isLinkSubscribe(long chatId, String link) {
        return webClient.post()
                .uri(TG_CHAT_LINK_SUBSCRIBE_EXISTS)
                .bodyValue(new LinkRecords.LinkExistRequest(chatId, link))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new LinkSubscribeException("Подписка на ссылку уже оформлена : " + link)))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Ошибка поиска ссылки :{}:{}", link, e.getMessage());
                    return Mono.just(false);
                });
    }


}
