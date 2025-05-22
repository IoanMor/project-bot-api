package me.ivanmorozov.scrapper.services.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.StockServiceException;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.client.TelegramBotClient;
import me.ivanmorozov.scrapper.services.db.ChatService;
import me.ivanmorozov.scrapper.services.db.StockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPriceStockService {
   private final ChatService chatService;
   private final StockService stockService;
   private final StockApiClient stockApiClient;
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

                      return stockApiClient.getPrice(ticker)
                              .filter(price -> price.compareTo(BigDecimal.ZERO)>=0)
                              .flatMap(price->{
                                  String msg = String.format("%s - цена = %2f", ticker,price);
                                  return botClient.sendReactiveMsg(chatId,msg);
                              })
                              .onErrorResume(e->{
                                  log.error("Ошибка обработки акции {}: {}", ticker, e.getMessage());
                                  return Mono.empty();
                              })
                              .subscribeOn(Schedulers.boundedElastic());

              }).then();
    }

}
