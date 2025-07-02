package me.ivanmorozov.telegrambot.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackStockCommand implements BotCommandHandler {
    private final TelegramKafkaProducer kafkaProducer;
    private final MessageWrapper messageWrapper;


    @Override
    public String getCommand() {
        return "/tstock";
    }

    @Override
    public void execute(long chatId, String userName, String[] args) {

        if (args.length < 1) {
            messageWrapper.sendMessage(chatId, "ℹ️ Использование: /tstock <тикер_акции>").subscribe();
            return;
        }
        String ticker = args[0];

        if (ticker.isBlank() || ticker == null) {
            messageWrapper.sendMessage(chatId, "❌ Неверный формат. Пример: /tStock SBER (пример акции Сбербанк)").subscribe();
            return;
        }
        try {
            kafkaProducer.sendSubscribeStockRequest(chatId, ticker);
        } catch (Exception e) {
            log.error("Ошибка при подписке на акцию {}|{}", ticker, e.getMessage());
            messageWrapper.sendMessage(chatId, "⚠️ При подписке что-то пошло не так").subscribe();
        }
    }
}
