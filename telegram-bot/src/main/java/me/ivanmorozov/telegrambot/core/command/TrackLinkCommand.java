package me.ivanmorozov.telegrambot.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.MessageTelegramClient;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;

import org.springframework.stereotype.Component;

import java.util.Optional;

import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.parseQuestionId;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackLinkCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageTelegramClient sendMessage;
    @Override
    public String getCommand() {
        return "/track";
    }

    @Override
    public void execute(long chatId, String userName, String[] args) {
        if (args.length < 1) {
            sendMessage.sendMessageClient(chatId, "ℹ️ Использование: /track <ссылка_на_вопрос>").subscribe();
            return;
        }
        String link = args[0];
        Optional<Long> questionIdOp = parseQuestionId(link);
        if (questionIdOp.isEmpty()) {
            sendMessage.sendMessageClient(chatId, "❌ Неверный формат ссылки. Пример: /track https://stackoverflow.com/questions/12345").subscribe();
            return;
        }
        try {
            kafkaProducer.sendSubscribeLinkRequest(chatId, link);
        } catch (Exception e) {
            log.error("Ошибка подписки chatId={}: {}", chatId, e.getMessage());
            sendMessage.sendMessageClient(chatId, "⚠️ Временная ошибка сервера").subscribe();
        }
    }
}
