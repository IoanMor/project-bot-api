package me.ivanmorozov.telegrambot.service;


import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StackExchangeClient;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramBotConfig botConfig;
    private final ScrapperApiClient client;
    private final StackExchangeClient stackClient;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public TelegramBotService(TelegramBotConfig botConfig, ScrapperApiClient client, StackExchangeClient stackClient) {
        this.botConfig = botConfig;
        this.client = client;
        this.stackClient = stackClient;

        List<BotCommand> listCommand = new ArrayList<>();
        listCommand.add(new BotCommand("/start", "Начните работу с ботом"));
        listCommand.add(new BotCommand("/track", "Подписаться на новости по ссылке"));
        listCommand.add(new BotCommand("/untrack", "Отписаться от новостей"));
        listCommand.add(new BotCommand("/help", "Информация и помощь"));
        listCommand.add(new BotCommand("/list", "Показать все отслеживаемые ссылки"));
        try {
            this.execute(new SetMyCommands(listCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка обработки команды: {}", e.getMessage());
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            try {
                if (msg.startsWith("/track")){
                    handleTrackCommand(update,msg,chatId);
                    return;
                }

                switch (msg) {
                    case "/start" -> startCommand(chatId, userName);
                    case "/help" -> sendMsg(chatId, HELP_TEXT);
                    default -> sendMsg(chatId, "Команда не найдена :(");
                }
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void startCommand(long chatId, String userName) throws TelegramApiException {
        String safeName = userName != null ? userName : "пользователь";
        sendMsg(chatId, "Приветствую, " + safeName + "...");

        try {
            boolean isRegistered = Boolean.TRUE.equals(client.isChatRegister(chatId)
                    .block(Duration.ofSeconds(5)));

            if (isRegistered) {
                sendMsg(chatId, "ℹ️ Вы уже зарегистрированы.");
            } else {
                sendMsg(chatId, "🔄 Вы еще не зарегистрированы. Выполняю регистрацию...");

                boolean success = Boolean.TRUE.equals(client.registerChat(chatId)
                        .block(Duration.ofSeconds(5)));

                String resultMsg = success ? START_TEXT : "⚠️ Не удалось зарегистрировать. Попробуйте позже.";
                sendMsg(chatId, resultMsg);
            }
        } catch (Exception e) {
            sendMsg(chatId, "❌ Произошла ошибка при попытке регистрации.");
            log.error("Ошибка регистрации: id " + chatId + "/ msg - " + e.getMessage());
        }
    }

    private void trackCommand(Update update,long chatId, long questionID) throws TelegramApiException {
        try {
            String userName = update.getMessage().getChat().getFirstName();
            boolean isTrack = stackClient.trackLink(questionID)
                    .timeout(Duration.ofSeconds(5))
                    .blockOptional()
                    .orElse(false);
            String link = "https://stackoverflow.com/questions/"+questionID;
            if (isTrack) {
                String answer = "✅ Вы подписаны на обновления: " + link;
                sendMsg(chatId, answer);
                log.info("Пользователь {} (chatId={}) подписался на вопрос {}",
                        userName, chatId, questionID);
            } else {
                String errorMsg = "❌ Не удалось подписаться на вопрос";
                log.error("{}: chatId={}, questionId={}", errorMsg, chatId, questionID);
                sendMsg(chatId, errorMsg);
            }
        } catch  (RuntimeException e) {
            log.error("Ошибка подписки chatId={}, questionId={}: {}",
                    chatId, questionID, e.getMessage());
            sendMsg(chatId, "⚠️ Ошибка сервера, попробуйте позже");
        }
    }

    private void handleTrackCommand(Update update,String linkMsg, long chatId) throws TelegramApiException {

        String[] parts = linkMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /track <ссылка_на_вопрос>");
            return;
        }
            String link = parts[1].trim();
        boolean isSubscribe = client.subscribeLink(chatId,link).timeout(Duration.ofSeconds(5)).blockOptional().orElse(false);
            long questionID = Long.parseLong(link.replaceAll("\\D+",""));
            log.info("парсинг id вопроса-" + questionID+" /success/");
            if (isSubscribe) {
                trackCommand(update, chatId, questionID);
            } else {log.error("chat id - {} | Не удалось сохранить ссылку {} ", chatId,link );}

    }


    private void unTrackCommand(long chatId, String link) throws TelegramApiException {
        String answer = "Вы отписались от получение новостей по ссылке: " + link;

        // логика отпописки

        sendMsg(chatId, answer);
    }


    private void sendMsg(long chatId, String textSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException te) {
            log.error("ERROR / : " + te.getMessage());
        }
    }

    private Mono<Void> sendReactiveMsg(long chatId, String textSend) {
        return Mono.fromRunnable(() -> sendMsg(chatId, textSend))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    final String START_TEXT = """
            ✅ Регистрация прошла успешно!
            \uD83D\uDCE2 *Как использовать?*
            1. Отправьте `/track https://example.com`
            2. Бот будет присылать уведомления при изменениях \s
            """;
    final String HELP_TEXT = "\uD83D\uDCDA *Справка по боту*\n" +
            "\n" +
            "Основные команды:\n" +
            "`/start` - Начать работу с ботом\n" +
            "`/help` - Показать эту справку\n" +
            "`/track <ссылка>` - Добавить ссылку для отслеживания\n" +
            "`/untrack <ссылка>` - Удалить ссылку из отслеживания\n" +
            "`/list` - Показать все отслеживаемые ссылки\n" +
            "`/settings` - Настройки уведомлений\n" +
            "\n" +
            "\uD83D\uDCE2 *Как использовать?*\n" +
            "1. Отправьте `/track https://example.com`\n" +
            "2. Бот будет присылать уведомления при изменениях  \n" +
            "\n" +
            "\uD83D\uDEE0 *Поддержка*:\n" +
            "А кому сейчас легко?\n" +
            "Не работает? Ну и хуй бы с ним!";
}
