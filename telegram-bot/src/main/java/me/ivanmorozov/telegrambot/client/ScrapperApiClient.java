package me.ivanmorozov.telegrambot.client;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.repositories.TgChatRepository;
import me.ivanmorozov.common.endpoints.ScrapperEndpoints;
import me.ivanmorozov.common.exception.ChatAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class ScrapperApiClient {
    private final WebClient webClient;
    private final Duration TIMEOUT = Duration.ofSeconds(5);

    public ScrapperApiClient() {
        String TG_CHAT_URI = "http://localhost:9030";
        this.webClient = WebClient.create(TG_CHAT_URI);
    }

    public Mono<Boolean> registerChat(long chatId) {
        return webClient.post()
                .uri(ScrapperEndpoints.TG_CHAT_REGISTER)
                .bodyValue(new TgChatRepository.ChatRegisterRequest(chatId))
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
                .onErrorResume(e->{
                    log.error("Ошибка регистрации чата {}: {}", chatId, e.getMessage());
                    return Mono.just(false);
                });
    }


    public Mono<Boolean> isChatRegister(long chatId) {
       return webClient.post()
               .uri(ScrapperEndpoints.TG_CHAT_EXISTS)
               .bodyValue(new TgChatRepository.ChatExistsRequest(chatId))
               .retrieve()
               .onStatus(HttpStatus.CONFLICT::equals,
                       response-> Mono.error(new ChatAlreadyExistsException(chatId)))
               .bodyToMono(Boolean.class)
               .timeout(TIMEOUT)
               .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
               .onErrorResume(e->{
                   log.error("Ошибка поиска чата {}:{}", chatId, e.getMessage());
                   return Mono.just(false);

               });
    }
}
