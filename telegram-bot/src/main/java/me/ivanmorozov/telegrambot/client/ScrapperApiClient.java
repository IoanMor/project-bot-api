package me.ivanmorozov.telegrambot.client;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.*;
import me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.LinkRecords;

import me.ivanmorozov.common.endpoints.ScrapperEndpoints;
import me.ivanmorozov.common.records.StockRecords;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import static me.ivanmorozov.common.endpoints.ScrapperEndpoints.*;
import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.*;

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
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ChatRegisterException("Ошибка регистрации чата. Статус: " + response.statusCode() +
                                ", Body: " + body))))
                .toBodilessEntity()
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .thenReturn(true)
                .onErrorResume(e -> {
                    if (e instanceof ChatRegisterException) {
                        return Mono.error(e);
                    }
                    return Mono.error(new ChatRegisterException("Ошибка регистраци"));
                });
    }

    public Mono<Set<Long>> getAllChats() {
        return webClient.post()
                .uri(TG_CHAT_GET_ALL_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<Long>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.error("Ошибка при получении чатов. Тип: {}, Сообщение: {}",
                            e.getClass().getSimpleName(), e.getMessage());
                    return Mono.just(Set.of());
                });
    }

    public Mono<Boolean> isChatRegister(long chatId) {
        return webClient.post()
                .uri(TG_CHAT_EXISTS)
                .bodyValue(new ChatRecords.ChatExistsRequest(chatId))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new ChatAlreadyExistsException("Ошибка поичка чата: " + chatId)))
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

    public Mono<Set<String>> getAllLinks(long chatId) {
        return webClient.post()
                .uri(TG_CHAT_GET_ALL_LINK)
                .bodyValue(new LinkRecords.LinkGetRequest(chatId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.error("Ошибка в выдаче всех подписок из чата {},{}", chatId, e.getMessage());
                    return Mono.just(Set.of());
                });
    }

    public Mono<Boolean> isLinkSubscribe(long chatId, String link) {
        String normalizedLink = normalizeLink(link);
        return webClient.post()
                .uri(TG_CHAT_LINK_SUBSCRIBE_EXISTS)
                .bodyValue(new LinkRecords.LinkExistRequest(chatId, normalizedLink))
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

    public Mono<Boolean> unsubscribeLink(long chatId, String link) {
        return webClient.post()
                .uri(TG_CHAT_DELL_LINK)
                .bodyValue(new LinkRecords.LinkSubscribeRequest(chatId, link))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new LinkSubscribeException("Не удалось отписаться от ссылки:" + link)))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Не удалось отписаться от ссылки: {}, {}", link, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> subscribeStock(long chatId, String ticker) {
        return webClient.post()
                .uri(TG_STOCK_SUBSCRIBE)
                .bodyValue(new StockRecords.StockSubscribeRequest(chatId, ticker))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new StockSubscribeException("Ошибка при подписке")))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Не удалось подписаться на акцияю: -chat ID- = {}, -ticker- = {}", chatId, ticker);
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> isExistStock(long chatId, String ticker) {
       return webClient.post()
                .uri(TG_CHAT_EXISTS)
                .bodyValue(new StockRecords.StockExistRequest(chatId, ticker))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new StockAlreadyExist("Ошибка в посике акции")))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Не удалось подписаться на акцияю: -chat ID- = {}, -ticker- = {}", chatId, ticker);
                    return Mono.just(false);
                });
    }

    public Mono<Set<String>> getSubscribeStock(long chatId){
        return webClient.post()
                .uri(TG_STOCK_GET_SUBSCRIPTIONS)
                .bodyValue(new StockRecords.StockGetRequest(chatId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Ошибка поиска акций :{}:{}", chatId, e.getMessage());
                    return Mono.just(Set.of());
                });
    }

    public Mono<Boolean> unSubscribeStock(long chatId, String ticker){
        return webClient.post()
                .uri(TG_STOCK_UNSUBSCRIBE)
                .bodyValue(new StockRecords.StockSubscribeRequest(chatId, ticker))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new StockSubscribeException("Ошибка при отписке акции")))
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(e -> {
                    log.error("Не удалось отписаться от акцияю: -chat ID- = {}, -ticker- = {}", chatId, ticker);
                    return Mono.just(false);
                });
    }
}
