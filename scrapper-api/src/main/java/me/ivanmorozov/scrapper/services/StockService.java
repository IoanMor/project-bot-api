package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.model.SubscribeStock;
import me.ivanmorozov.scrapper.repositories.StockRepository;
import me.ivanmorozov.scrapper.repositories.old.StockRepositories;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {
    private final StockRepository stockRepository;

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

    public boolean isSubscribed(long chatId, String ticker) {
        return stockRepository.existsByTicker(chatId, ticker);
    }

    public Set<String> getSubscriptions(long chatId) {
        return stockRepository.getTickersByChatId(chatId);
    }
}