package me.ivanmorozov.scrapper.services.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.LinkServiceException;

import me.ivanmorozov.scrapper.repositories.LinkRepository;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {
    private final LinkRepository linkRepository;

    public boolean subscribe(long chatId, String link) {
        try {
            linkRepository.insertLink(chatId, link);
            return true;
        } catch (Exception e) {
            log.error("Ошибка подписки чата {} на ссылку: {} \n {} ", chatId, link, e.getMessage());
            return false;
        }
    }

    public boolean unSubscribe(long chatId, String link) {
        try {
            linkRepository.removeLink(chatId, link);
            return true;
        } catch (Exception e) {
            log.error("Ошибка отписки чата {} от ссылки:{} \n {}", chatId, link, e.getMessage());
            return false;

        }
    }

    public Set<String> getAllSubscribeLinks(long chatId) {
        try {
            return linkRepository.getLinks(chatId);
        } catch (Exception e) {
            log.error("Ошибка в получении подписок чата:{} \n {}", chatId, e.getMessage());
            throw new LinkServiceException("Ошибка выдачи подписок чата " + chatId, e);
        }
    }

    public boolean isSubscribed(long chatId, String link) {
        return linkRepository.existsByLink(chatId, link);
    }


    public void updateCountAnswer(long chatId, String link, int count) {
        linkRepository.updateCountAnswer(chatId, link, count);
    }

    public int getCountAnswer(long chatId, String link) {
        return linkRepository.getCountAnswer(chatId, link);
    }
}


