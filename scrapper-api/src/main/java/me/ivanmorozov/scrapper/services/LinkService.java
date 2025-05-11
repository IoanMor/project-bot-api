package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.repositories.UserSubsLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {
    private final UserSubsLinkRepository linkRepository;

    public boolean addLink(long chatId, String link) {
        try {
            return linkRepository.addSubscription(chatId, link);
        } catch (Exception e) {
            log.error("Ошибка добавления подписки: chatId={}, link={}", chatId, link, e);
            return false;
        }
    }

    public Set<String> getLinks(long chatId) {
        return linkRepository.getSubscription(chatId);
    }

    public boolean linkExist(long chatId, String link) {
        return linkRepository.exists(chatId, link);
    }

    public boolean unSubscribe(long chatId, String link) {
        return linkRepository.removeSubscription(chatId,link);
    }


}
