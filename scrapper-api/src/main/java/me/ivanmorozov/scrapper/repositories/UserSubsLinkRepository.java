package me.ivanmorozov.scrapper.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class UserSubsLinkRepository {
    private final ConcurrentHashMap<Long, Set<String>> chatRepositories = new ConcurrentHashMap<>();

    public boolean addSubscription(Long chatId, String link) {
        if (!validationLink(link)) {
            return false;
        }
        Set<String> links = chatRepositories.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet());
        boolean added = links.add(normalizeLink(link));
        if (added) {
            log.info("Добавлена подписка: chatId={}, link={}", chatId, link);
        }
        return added;
    }

    public boolean removeSubscription(long chatId, String link) {
        Set<String> links = chatRepositories.get(chatId);
        if (links != null) {
            boolean removed = links.remove(normalizeLink(link));
            if (removed) {
                log.info("Удалена подписка: chatId={}, link={}", chatId, link);
            }
            return removed;
        }
        return false;
    }

    public boolean exists(long chatId, String link){
        Set<String> existingLinks = chatRepositories.get(chatId);
        if (existingLinks==null){
            return false;
        }
       return existingLinks.contains(link);
    }

    public Set<String> getSubscription(long chatId){
        return chatRepositories.getOrDefault(chatId, Collections.emptySet());
    }

    public boolean validationLink(String link) {
        return link != null && link.matches("https://stackoverflow\\.com/questions/\\d+.*");
    }
    private String normalizeLink(String link) {
        return link.replaceAll("(?i)/questions/(\\d+).*", "/questions/$1");
    }

    public record LinkExistRequest(long chatId, String link){}
    public record LinkSubscribeRequest(long chatId, String link){}

}
