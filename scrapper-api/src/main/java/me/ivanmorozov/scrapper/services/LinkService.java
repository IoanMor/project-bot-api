package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.model.SubscribeLink;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.repositories.old.UserSubsLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {
    private final LinkRepository linkRepository;

}
