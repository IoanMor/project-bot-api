package me.ivanmorozov.scrapper.client;

import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.SendChatException;
import me.ivanmorozov.common.records.TelegramBotRecords;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static me.ivanmorozov.common.endpoints.ScrapperEndpoints.*;

@Component
@Slf4j
public class TelegramBotClient {
    private final WebClient webClient;
    private final Duration TIMEOUT = Duration.ofSeconds(5);

    public TelegramBotClient() {
        this.webClient = WebClient.create(TG_BOT_URI);
    }

    public Mono<Void> sendReactiveMsg(long chatId, String msg) {
        return webClient.post()
                .uri(TG_BOT_SEND_MESSAGE)
                .bodyValue(new TelegramBotRecords.TgMessageRequest(chatId,msg))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(TIMEOUT)
                .retryWhen(Retry.backoff(3,Duration.ofMillis(100)))
                .onErrorResume(e ->{
                    log.error("Ошибка при отправке сообщения в TelegramBot", e);
                   if (e instanceof SendChatException){
                       return Mono.error(e);
                   }
                   return Mono.error(new SendChatException("Ошибка отправки сообщения из сервиса scrapper ", e));
                });

    }

}
