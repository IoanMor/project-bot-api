package me.ivanmorozov.scrapper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.LinkRecords;

import me.ivanmorozov.common.records.StockRecords;

import me.ivanmorozov.common.endpoints.ScrapperEndpoints;

import me.ivanmorozov.scrapper.client.StackExchangeClient;
import me.ivanmorozov.scrapper.services.ChatService;
import me.ivanmorozov.scrapper.services.LinkService;
import me.ivanmorozov.scrapper.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TgChatController {
    private final ChatService chatService;
    private final LinkService linkService;
    private final StockService stockService;


    @PostMapping(ScrapperEndpoints.TG_CHAT_REGISTER)
    public ResponseEntity<Void> registrationChat(@RequestBody ChatRecords.ChatRegisterRequest request) {
        chatService.registerChat(request.chatId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_EXISTS)
    public ResponseEntity<Boolean> isChatExist(@RequestBody ChatRecords.ChatExistsRequest request) {
        return ResponseEntity.ok(chatService.isChatExist(request.chatId()));
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_GET_ALL_REGISTER)
    public ResponseEntity<Set<Long>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllRegisteredChat());
    }

    @PostMapping(ScrapperEndpoints.TG_LINK_SUBSCRIBE)
    public ResponseEntity<Boolean> subscribeToLink(@RequestBody LinkRecords.LinkSubscribeRequest request) {
        return ResponseEntity.ok(linkService.subscribe(request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_GET_ALL_LINK)
    public ResponseEntity<Set<String>> getAllSubscribeLinks(@RequestBody LinkRecords.LinkGetRequest request) {
        return ResponseEntity.ok(linkService.getAllSubscribeLinks(request.chatId()));
    }

    @PostMapping(ScrapperEndpoints.TG_LINK_SUBSCRIBE_EXISTS)
    public ResponseEntity<Boolean> isLinkExist(@RequestBody LinkRecords.LinkExistRequest request) {
        return ResponseEntity.ok(linkService.isSubscribed(request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_DELL_LINK)
    public ResponseEntity<Boolean> unsubscribeFromLink(@RequestBody LinkRecords.LinkSubscribeRequest request) {
        return ResponseEntity.ok(linkService.unSubscribe(request.chatId(), request.link()));
    }

    @PostMapping(ScrapperEndpoints.TG_GET_COUNT_ANSWER)
    public ResponseEntity<Integer> getCountSubscribeAnswer(@RequestBody LinkRecords.LinkGetCountAnswerRequest request) {
        return ResponseEntity.ok(linkService.getCountAnswer(request.chatId(),request.link()));
    }


    @PostMapping(ScrapperEndpoints.TG_STOCK_SUBSCRIBE)
    public ResponseEntity<Boolean> subscribeToStock(@RequestBody StockRecords.StockSubscribeRequest request) {
        return ResponseEntity.ok(stockService.subscribe(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_CHECK_EXISTS)
    public ResponseEntity<Boolean> isStockExist(@RequestBody StockRecords.StockExistRequest request) {
        return ResponseEntity.ok(stockService.isSubscribed(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_UNSUBSCRIBE)
    public ResponseEntity<Boolean> unSubscribeFromStock(@RequestBody StockRecords.StockSubscribeRequest request) {
        return ResponseEntity.ok(stockService.unsubscribe(request.chatId(), request.ticker()));
    }

    @PostMapping(ScrapperEndpoints.TG_STOCK_GET_SUBSCRIPTIONS)
    public ResponseEntity<Set<String>> getAllSubscribeStock(@RequestBody StockRecords.StockGetRequest request) {
        return ResponseEntity.ok(stockService.getSubscriptions(request.chatId()));
    }

}