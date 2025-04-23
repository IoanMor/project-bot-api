package me.ivan.morozov.scrapperapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivan.morozov.scrapperapi.repositories.TgChatRepository;
import me.ivanmorozov.endpoints.ScrapperEndpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TgChatController {
    private final TgChatRepository repository;

    @PostMapping(ScrapperEndpoints.TG_CHAT_REGISTER)
    public ResponseEntity<Void> registerChat(@RequestBody TgChatRepository.ChatRegisterRequest request) {
        repository.add(request.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping(ScrapperEndpoints.TG_CHAT_EXISTS)
    public ResponseEntity<Boolean> existsChat(@RequestBody TgChatRepository.ChatExistsRequest request) {
        return ResponseEntity.ok(repository.exist(request.id()));
    }
}