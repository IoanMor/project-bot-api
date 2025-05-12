package me.ivanmorozov.scrapper.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class StockRepositories {
    public final ConcurrentHashMap<Long, Set<String>> stockSubscription = new ConcurrentHashMap<>();

    public boolean addStock(long chatId, String ticker) {
    return stockSubscription.compute(chatId, (k, existingSet)-> {
        Set<String> stock = (existingSet!=null)? existingSet : ConcurrentHashMap.newKeySet();
        boolean added = stock.add(ticker);
        if (added) log.info("Акция добавлена " + ticker);
        return stock;
    }).contains(ticker);
    }
    public boolean existStock(long chatId, String ticker){
        Set<String> stock = stockSubscription.get(chatId);
        if (stock==null){
            return false;
        }
        return stock.contains(ticker);
    }

    public boolean removeStock(long chatId, String ticker){
        Set<String> stock = stockSubscription.get(chatId);
        if (stock!=null){
            boolean removed = stock.remove(ticker);
            if (removed) {
                log.info("Удалена акция: chatId={}, ticker={}", chatId, ticker);
            }
            return removed;
        }
        return false;

    }

    public Set<String> getStock (long chatId){
     return stockSubscription.getOrDefault(chatId, Collections.emptySet());
    }
}
