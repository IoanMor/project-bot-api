package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.scrapper.repositories.UserSubsLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final UserSubsLinkRepository linkRepository;

    public boolean addLink(long chatId, String link) {
        return linkRepository.addSubscription(chatId, link);
    }
    public Set<String> getLinks (long chatId){
       return linkRepository.getSubscription(chatId);
    }
    public boolean linkExist (long chatId, String link) {
        return linkRepository.exists(chatId,link);
    }




}
