package me.ivanmorozov.telegrambot.service;


import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import me.ivanmorozov.telegrambot.core.CommandDispatcher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.*;


@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramBotConfig botConfig;
    private final CommandDispatcher commandDispatcher;
    private final TelegramSendMessage telegramSendMessage;
    private final RegistrationService registrationService;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public TelegramBotService(TelegramBotConfig botConfig, CommandDispatcher commandDispatcher, TelegramSendMessage telegramSendMessage, RegistrationService registrationService) {
        this.botConfig = botConfig;
        this.commandDispatcher = commandDispatcher;
        this.telegramSendMessage = telegramSendMessage;
        this.registrationService = registrationService;


        List<BotCommand> listCommand = new ArrayList<>();
        listCommand.add(new BotCommand("/start", "Начните работу с ботом"));
        listCommand.add(new BotCommand("/track", "Подписаться на новости по ссылке"));
        listCommand.add(new BotCommand("/untrack", "Отписаться от новостей"));
        listCommand.add(new BotCommand("/tstock", "Подписаться на получение цены акции"));
        listCommand.add(new BotCommand("/utstock", "Отписаться от получении цены акции"));
        listCommand.add(new BotCommand("/links", "Показать все отслеживаемые ссылки"));
        listCommand.add(new BotCommand("/stock", "Показать все отслеживаемые акции"));
        listCommand.add(new BotCommand("/help", "Информация и помощь"));
        try {
            this.execute(new SetMyCommands(listCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка обработки команды: {}", e.getMessage());
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        try {
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            if (msg.startsWith("/start")) {
                commandDispatcher.dispatch(msg, chatId, userName);
                return;
            }

            if (!isChatRegister(chatId)) {
                telegramSendMessage.sendMessage(this, chatId, "⛔ Для использования бота необходимо зарегистрироваться!\nВведите команду /start");
                return;
            }

            commandDispatcher.dispatch(msg, chatId, userName);

        } catch (Exception e) {
            log.error("Ошибка обработки команды: {}", e.getMessage());
            if (update.hasMessage()) {
                sendMessage(update.getMessage().getChatId(), "⚠️ Произошла ошибка при обработке команды");
            }
        }
    }

    public void sendMessage(long chatId, String textSend) {
      telegramSendMessage.sendMessage(this,chatId, textSend);
    }

    public boolean isChatRegister(long chatId) throws TelegramApiException {
       return registrationService.isChatRegister(chatId,this);
    }





}
