package me.ivanmorozov.telegrambot.client;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static me.ivanmorozov.common.apiUrl.APIUrl.TELEGRAM_API_URL;

@Component
@Slf4j
public class MessageTelegramClient {
    private final WebClient webClient;
    private final TelegramBotConfig config;

    public MessageTelegramClient(@Qualifier("telegramAPIClient") WebClient webClient, TelegramBotConfig config) {
        this.config = config;
        this.webClient  = webClient;
    }

    public Mono<Void> sendMessageClient(long chatId, String text) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/bot{token}/sendMessage")
                        .queryParam("chat_id", chatId)
                        .queryParam("text", text)
                        .build(config.getToken()))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3,Duration.ofMillis(200)))
                .doOnError(e -> log.error("Ошибка API Telegram при отправке сообщения в чат {}: {}", chatId, e.getMessage()));
    }
}
