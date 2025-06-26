package me.ivanmorozov.telegrambot.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
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
