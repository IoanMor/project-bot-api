package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.client.TelegramBotClient;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StockApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceStockService {
   private final ChatService chatService;
   private final StockService stockService;
   private final TelegramBotClient botClient;

    @Scheduled(cron = "0 30 9 * * ?")
    public void sendTimeStock(){
        chatService.getAllChatsWithRetry()
                .flatMap(this::checkSubscribeStock).subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(()-> log.info("=Проверка чатов завершена="))
                .subscribe(
                        null,
                        error -> log.error("Фатальная ошибка при проверке: {}", error.getMessage())
                );
    }

    public Mono<Void> checkSubscribeStock(long chatId) {
      return Flux.fromIterable(stockService.getSubscriptions(chatId))
              .flatMap(ticker -> {
                  try {
                      return
                  }
              })
    }

}
