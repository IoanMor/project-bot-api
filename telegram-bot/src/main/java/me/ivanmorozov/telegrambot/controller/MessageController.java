package me.ivanmorozov.telegrambot.controller;

import lombok.RequiredArgsConstructor;

import me.ivanmorozov.common.records.TelegramBotRecords;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static me.ivanmorozov.common.endpoints.ScrapperEndpoints.*;


@RestController
@RequiredArgsConstructor
public class MessageController {
    private final TelegramBotService telegramBotService;

    @PostMapping(TG_BOT_SEND_MESSAGE)
    public ResponseEntity<Void> sendMessage(@RequestBody TelegramBotRecords.TgMessageRequest request) {
        telegramBotService.sendReactiveMsg(request.chatId(), request.msg()).timeout(Duration.ofSeconds(5)).block();
        return ResponseEntity.ok().build();
    }
}

