package me.ivanmorozov.scrapper.repositories.old;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.*;

@Repository
@Slf4j
public class UserSubsLinkRepository {
    private final ConcurrentHashMap<Long, Set<String>> userSubsLinks = new ConcurrentHashMap<>();

    public boolean addSubscription(Long chatId, String link) {
        if (!validationLink(link)) {
            log.warn("Чат - {} - передал невалидную ссылку: {}", chatId, link);
            return false;
        }
        return userSubsLinks.compute(chatId, (k, existingSet) -> {
            Set<String> links = (existingSet != null) ? existingSet : ConcurrentHashMap.newKeySet();
            boolean added = links.add(normalizeLink(link));
            if (added) log.info("Подписка добавлена");
            return links;
        }).contains(normalizeLink(link));

    }

    public void debugGetUserLinkRep() { // потом удалить
        System.out.println(userSubsLinks);
    }

    public boolean removeSubscription(long chatId, String link) {
        Set<String> links = userSubsLinks.get(chatId);
        if (links != null) {
            boolean removed = links.remove(normalizeLink(link));
            if (removed) {
                log.info("Удалена подписка: chatId={}, link={}", chatId, link);
            }
            return removed;
        }
        return false;
    }

    public boolean exists(long chatId, String link) {
        Set<String> existingLinks = userSubsLinks.get(chatId);
        if (existingLinks == null) {
            return false;
        }
        return existingLinks.contains(link);
    }

    public Set<String> getSubscription(long chatId) {
        return userSubsLinks.getOrDefault(chatId, Collections.emptySet());
    }


}
