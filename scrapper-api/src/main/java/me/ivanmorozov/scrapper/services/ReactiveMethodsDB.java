package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReactiveMethodsDB {
    private final TelegramChatRepository chatRepository;

    public Flux<Long> getAllChatsWithRetry() {
        return Mono.fromCallable(chatRepository::getAllChats)
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .flatMapMany(Flux::fromIterable);
    }
}
