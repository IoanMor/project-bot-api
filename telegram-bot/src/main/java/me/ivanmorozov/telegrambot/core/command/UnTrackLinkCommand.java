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
public class UnTrackLinkCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageWrapper messageWrapper;
    @Override
    public String getCommand() {
        return "/untrack";
    }

    @Override
    public void execute(long chatId, String userName, String[] args) {
        if (args.length < 1) {
            messageWrapper.sendMessage(chatId, "ℹ️ Использование: /untrack <ссылка_на_вопрос>").subscribe();
            return;
        }
        String link = args[0];
        try {
            kafkaProducer.sendUnSubscribeLinkRequest(chatId, link);
            messageWrapper.sendMessage(chatId, "⌛ Идёт отписка...").subscribe();
        } catch (Exception e) {
            log.error("Ошибка отписки chatId={}: {}", chatId, e.getMessage());
            messageWrapper.sendMessage(chatId, "⚠️ Временная ошибка сервера").subscribe();
        }
    }
}
