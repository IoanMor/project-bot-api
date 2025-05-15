package me.ivanmorozov.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.LinkRecords;

import me.ivanmorozov.common.records.StockRecords;
import me.ivanmorozov.scrapper.repositories.old.TgChatRepository;
import me.ivanmorozov.common.endpoints.ScrapperEndpoints;

import me.ivanmorozov.scrapper.services.LinkService;
import me.ivanmorozov.scrapper.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TgChatController {
    private final TgChatRepository chatRepository;
    private final LinkService linkService;
    private final StockService stockService;


    @PostMapping(ScrapperEndpoints.TG_CHAT_REGISTER)
    public ResponseEntity<Void> registerChat(@RequestBody ChatRecords.ChatRegisterRequest request) {
        chatRepository.add(request.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_EXISTS)
    public ResponseEntity<Boolean> existsChat(@RequestBody ChatRecords.ChatExistsRequest request) {
        return ResponseEntity.ok(chatRepository.exist(request.id()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_GET_ALL_REGISTER)
    public ResponseEntity<Set<Long>> getAllChats() {
        return ResponseEntity.ok(chatRepository.getAllChats());
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE)
    public ResponseEntity<Boolean> subscribeLink(@RequestBody LinkRecords.LinkSubscribeRequest request) {
        boolean result = linkService.addLink(request.chatId(), request.link());
        return ResponseEntity.ok(result);
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_GET_ALL_LINK)
    public ResponseEntity<Set<String>> getUserLink(@RequestBody LinkRecords.LinkGetRequest request) {
        return ResponseEntity.ok(linkService.getLinks(request.chatId()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_LINK_SUBSCRIBE_EXISTS)
    public ResponseEntity<Boolean> existLinks(@RequestBody LinkRecords.LinkExistRequest request) {
        return ResponseEntity.ok(linkService.linkExist(request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_DELL_LINK)
    public ResponseEntity<Boolean> dellLink(@RequestBody LinkRecords.LinkSubscribeRequest request) {
        return ResponseEntity.ok(linkService.unSubscribe(request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_SUBSCRIBE)
    public ResponseEntity<Boolean> subscribeOnStock(@RequestBody StockRecords.StockSubscribeRequest request) {
        return ResponseEntity.ok(stockService.subscribeOnStock(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_CHECK_EXISTS)
    public ResponseEntity<Boolean> existStock(@RequestBody StockRecords.StockExistRequest request){
        return ResponseEntity.ok(stockService.isStock(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_UNSUBSCRIBE)
    public ResponseEntity<Boolean> unSubscribeStock(@RequestBody StockRecords.StockSubscribeRequest request){
        return ResponseEntity.ok(stockService.unSubscribeStock(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_GET_SUBSCRIPTIONS)
    public ResponseEntity<Set<String>> getSubscribeStock(@RequestBody StockRecords.StockGetRequest request){
        return ResponseEntity.ok(stockService.getSubscribeStock(request.chatId()));
    }



}