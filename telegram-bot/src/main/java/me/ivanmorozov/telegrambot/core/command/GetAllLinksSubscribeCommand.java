package me.ivanmorozov.telegrambot.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetAllLinksSubscribeCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageWrapper messageWrapper;


    @Override
    public String getCommand() {
        return "/links";
    }

    @Override
    public void execute(long chatId, String userName, String[] args) {
        try {
            kafkaProducer.sendAllSubscribeLinksRequest(chatId);
        } catch (Exception e) {
            log.error("Произошла ошибка в классе GetAllLinksSubscribeCommand, [{}]", e.getMessage());
            messageWrapper.sendMessage(chatId, "⚠️ Временная ошибка сервера").subscribe();
        }
    }
}
