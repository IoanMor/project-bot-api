package me.ivanmorozov.scrapper.services.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.repositories.StockRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {
    private final StockRepository stockRepository;
    private final StockApiClient stockApiClient;

    public boolean subscribe(long chatId, String ticker) {
        try {
            stockRepository.insertStock(chatId, ticker);
            return true;
        } catch (Exception e) {
            log.error("Ошибка подписки чата {} на тикер {}: {}", chatId, ticker, e.getMessage());
            return false;
        }
    }

    public boolean unsubscribe(long chatId, String ticker) {
        try {
            stockRepository.removeStock(chatId, ticker);
            return true;
        } catch (Exception e) {
            log.error("Ошибка отписки чата {} от тикера {}: {}", chatId, ticker, e.getMessage());
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean isTickerSubscribed(long chatId, String ticker) {
        try {
            System.out.println("Метод isTickerSubscribed вызван для chatId=" + chatId + ", ticker=" + ticker);
            boolean exists = stockRepository.existsByTicker(chatId, ticker);
            System.out.println("Результат: " + exists);
            return exists;
        } catch (Exception e) {
            System.out.println("Ошибка в isTickerSubscribed: " + e.getMessage());
            throw e;
        }
    }

    public Set<String> getSubscriptions(long chatId) {
        return stockRepository.getTickersByChatId(chatId);
    }

    public BigDecimal getStockPrice(String ticker){
       return stockApiClient.getPrice(ticker).timeout(Duration.ofSeconds(5)).block();
    }
}