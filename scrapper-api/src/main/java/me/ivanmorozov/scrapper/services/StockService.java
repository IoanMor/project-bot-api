package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.scrapper.repositories.StockRepositories;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepositories stockRepositories;

    public boolean subscribeOnStock(long chatId, String ticker) {
        return stockRepositories.addStock(chatId, ticker);
    }

    public boolean isStock(long chatId, String ticker) {
        return stockRepositories.existStock(chatId, ticker);
    }

    public boolean unSubscribeStock(long chatId, String ticker) {
        return stockRepositories.removeStock(chatId, ticker);
    }

    public Set<String> getSubscribeStock(long chatId) {
        return stockRepositories.getStock(chatId);
    }
}
