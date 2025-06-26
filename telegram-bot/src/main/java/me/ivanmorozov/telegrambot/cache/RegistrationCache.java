package me.ivanmorozov.telegrambot.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class RegistrationCache {
    private final Cache<Long, Boolean> cache;

    public RegistrationCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(1,TimeUnit.HOURS)
                .maximumSize(10_000)
                .recordStats()
                .build();
    }
    public void setRegistered(long chatId, boolean isRegistered) {
        cache.put(chatId, isRegistered);
    }

    public Boolean isRegistered(long chatId) {
        return cache.getIfPresent(chatId);
    }

    public CacheStats getStats() {
        return cache.stats();
    }
}
