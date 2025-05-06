package me.ivanmorozov.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.LinkRecords;

import me.ivanmorozov.scrapper.repositories.TgChatRepository;
import me.ivanmorozov.common.endpoints.ScrapperEndpoints;

import me.ivanmorozov.scrapper.services.LinkService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TgChatController {
    private final TgChatRepository chatRepository;
    private final LinkService linkService;

    @PostMapping(ScrapperEndpoints.TG_CHAT_REGISTER)
    public ResponseEntity<Void> registerChat(@RequestBody ChatRecords.ChatRegisterRequest request) {
        chatRepository.add(request.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_EXISTS)
    public ResponseEntity<Boolean> existsChat(@RequestBody ChatRecords.ChatExistsRequest request) {
        return ResponseEntity.ok(chatRepository.exist(request.id()));
    }
    @PostMapping(value = ScrapperEndpoints.TG_CHAT_GET_ALL_REGISTER)
    public ResponseEntity<Set<Long>> getAllChats(){
        return ResponseEntity.ok(chatRepository.getAllChats());
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE)
    public ResponseEntity<Void> subscribeLink(@RequestBody LinkRecords.LinkSubscribeRequest request){
        linkService.addLink (request.chatId(), request.link());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE_EXISTS)
    public ResponseEntity<Boolean> existLinks (@RequestBody LinkRecords.LinkExistRequest request){
        return ResponseEntity.ok( linkService.linkExist (request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_GET_SUBSCRIBES)
    public ResponseEntity<Set<String>> getLinks (@RequestBody LinkRecords.LinkGetRequest request){
        return ResponseEntity.ok(linkService.getLinks(request.chatId()));
    }

}