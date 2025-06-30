package me.ivanmorozov.telegrambot.core;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class DispatcherBotCommand {
    public BotCommandHandler dispatcher(Update update){
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        try {
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            if (msg.startsWith("/start")) {
               return startCommand(chatId, userName);

            }

            if (!isChatRegister(chatId)) {
                sendMsg(chatId, "⛔ Для использования бота необходимо зарегистрироваться!\nВведите команду /start");
               return null;
            }

            switch (true) {
                case msg.startsWith("/track ") -> {
                    return handleTrackCommand(msg, chatId);
                }
                case msg.startsWith("/untrack ") -> unTrackCommand(chatId, msg);
                case msg.startsWith("/tstock ") -> trackStock(chatId, msg);
                case msg.startsWith("/utstock ") -> untrackStock(chatId, msg);
                case msg.equals("/stock") -> getAllStockSubscribes(chatId);
                case msg.equals("/links") -> getAllLinksSubscribes(chatId);
                case msg.equalsIgnoreCase("/help") -> sendMsg(chatId, HELP_TEXT);
                default -> sendMsg(chatId, "❌ Неизвестная команда. Введите /help для списка команд");
            }

        } catch (Exception e) {
            log.error("Ошибка обработки команды: {}", e.getMessage());
            if (update.hasMessage()) {
                try {
                    sendMsg(update.getMessage().getChatId(), "⚠️ Произошла ошибка при обработке команды");
                } catch (Exception ex) {
                    log.error("Не удалось отправить сообщение об ошибке: {}", ex.getMessage());
                }
            }
        }
    }
}
