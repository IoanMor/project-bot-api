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

import static me.ivanmorozov.common.constMsg.Msg.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                if (msg.startsWith("/track")) {
                    handleTrackCommand(update, msg, chatId);
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

    private void trackCommand(Update update, long chatId, Optional<Long> questionID) throws TelegramApiException {
        String link = "https://stackoverflow.com/questions/" + questionID;
        String userName = Optional.ofNullable(update.getMessage().getChat().getUserName()).orElse("UNKNOWN");

        boolean isTrack = stackClient.trackLink(questionID)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Ошибка при подписке chatId={}, questionId={}: {}", chatId, questionID, e.getMessage());
                    return Mono.just(false);
                })
                .blockOptional()
                .orElse(false);

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

    }

    private void handleTrackCommand(Update update, String linkMsg, long chatId) throws TelegramApiException {

        String[] parts = linkMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /track <ссылка_на_вопрос>");
            return;
        }
        String link = parts[1].trim();
        Optional<Long> questionIdOp = parseQuestionId(link);

        if (questionIdOp.isEmpty()) {
            sendMsg(chatId, "❌ Неверный формат ссылки. Пример: /track https://stackoverflow.com/questions/12345");
            return;
        }

        boolean isSubscribeExist = client.isLinkSubscribe(chatId, link)
                .timeout(Duration.ofSeconds(5))
                .blockOptional()
                .orElse(false);

        if (isSubscribeExist) {
            sendMsg(chatId, "ℹ️ Вы уже подписаны на эту ссылку.");
            return;
        }

        boolean isSubscribe = client.subscribeLink(chatId, link)
                .timeout(Duration.ofSeconds(5))
                .blockOptional()
                .orElse(false);

        if (!isSubscribe) {
            log.error("chat id - {} | Не удалось подписаться на ссылку {}", chatId, link);
            sendMsg(chatId, "❌ Не удалось подписаться на ссылку.");
            return;
        }

        log.info("парсинг id вопроса-" + questionIdOp + " /success/");
        trackCommand(update, chatId, questionIdOp);

    }


    private void unTrackCommand(long chatId, String link) throws TelegramApiException {
        String answer = "Вы отписались от получение новостей по ссылке: " + link;

        // логика отпописки

        sendMsg(chatId, answer);
    }


//    private void sendMsg(long chatId, String textSend) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(chatId);
//        sendMessage.setText(textSend);
//
//        try {
//            execute(sendMessage);
//        } catch (TelegramApiException te) {
//            log.error("ERROR / : " + te.getMessage());
//        }
//    }

    private Mono<Void> sendMsg(long chatId, String textSend) {
        return Mono.fromRunnable(() -> sendMsg(chatId, textSend))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Optional<Long> parseQuestionId(String link) {
        try {
            // Пример для StackOverflow
            Pattern pattern = Pattern.compile("stackoverflow\\.com/questions/(\\d+)");
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()) {
                return Optional.of(Long.parseLong(matcher.group(1)));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }


    }
}
