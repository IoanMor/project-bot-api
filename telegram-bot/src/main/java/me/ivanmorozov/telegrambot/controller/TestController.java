package me.ivanmorozov.telegrambot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final CheckSubscribeService checkService;
    private final ScrapperApiClient client;
    private final TelegramBotService tgService;
    private final StockApiClient stockApiClient;

    @PostMapping("/test/check-updates")
    public String testCheck() {
        checkService.checkUpdates();
        return "Проверка обновлений запущена";
    }

    @PostMapping("/test/send-notification")
    public String testNotification(@RequestParam Long chatId, @RequestParam String text) {
        tgService.sendReactiveMsg(chatId, text).subscribe();
        return "Уведомление отправлено";
    }

    @PostMapping("/test/send-dummy")
    public void testSend(@RequestParam Long chatId) {
        tgService.sendReactiveMsg(chatId, "Тестовое уведомление").block();
    }
    @GetMapping("/debug/links")
    public ResponseEntity<Set<String>> getLinks(
            @RequestParam("chatId") Long chatId) {  // Явно указываем имя параметра
        try {
            Set<String> links = client.getAllLinks(chatId).block();
            return ResponseEntity.ok(links);
        } catch (Exception e) {
            log.error("Ошибка получения ссылок для chatId={}", chatId, e);
            return ResponseEntity.internalServerError().build();
        }
    }


}