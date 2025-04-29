package me.ivanmorozov.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.scrapper.repositories.TgChatRepository;
import me.ivanmorozov.common.endpoints.ScrapperEndpoints;

import me.ivanmorozov.scrapper.repositories.UserSubsLinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TgChatController {
    private final TgChatRepository chatRepository;
    private final UserSubsLinkRepository linkRepository;

    @PostMapping(ScrapperEndpoints.TG_CHAT_REGISTER)
    public ResponseEntity<Void> registerChat(@RequestBody TgChatRepository.ChatRegisterRequest request) {
        chatRepository.add(request.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_EXISTS)
    public ResponseEntity<Boolean> existsChat(@RequestBody TgChatRepository.ChatExistsRequest request) {
        return ResponseEntity.ok(chatRepository.exist(request.id()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE)
    public ResponseEntity<Void> subscribeLink(@RequestBody UserSubsLinkRepository.LinkSubscribeRequest request){
        linkRepository.addSubscription(request.chatId(), request.link());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE_EXISTS)
    public ResponseEntity<Boolean> existLinks (@RequestBody UserSubsLinkRepository.LinkExistRequest request){
        return ResponseEntity.ok(linkRepository.exists(request.chatId(), request.link()));
    }

}