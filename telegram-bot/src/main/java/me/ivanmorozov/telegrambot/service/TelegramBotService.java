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
import java.util.*;
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
        listCommand.add(new BotCommand("/list", "Показать все отслеживаемые ссылки"));
        listCommand.add(new BotCommand("/help", "Информация и помощь"));
        try {
            this.execute(new SetMyCommands(listCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка обработки команды: {}", e.getMessage());
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String msg = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                String userName = update.getMessage().getChat().getFirstName();

                if (msg.startsWith("/start")) {
                    startCommand(chatId, userName);
                    return;
                }

                if (isChatRegister(chatId)) {
                    if (msg.startsWith("/track")) {
                        handleTrackCommand(update, msg, chatId);
                    } else if (msg.startsWith("/untrack")) {
                        unTrackCommand(chatId, msg);
                    } else if ("/help".equalsIgnoreCase(msg)) {
                        sendMsg(chatId, HELP_TEXT);
                    } else if (msg.equalsIgnoreCase("/list")){
                        getAllSubscribes(chatId);
                    }
                    else {
                        sendMsg(chatId, "❌ Неизвестная команда. Введите /help для списка команд");
                    }
                } else {
                    sendMsg(chatId, "⛔ Для использования бота необходимо зарегистрироваться!\nВведите команду /start");
                    return;
                }

            }

        } catch (Exception e) {
            log.error("Ошибка обработки команды", e);
            safelyNotifyUser(update);
        }
    }

    private void safelyNotifyUser(Update update) {
        try {
            if (update != null && update.hasMessage()) {
                sendMsg(update.getMessage().getChatId(),
                        "⚠ Техническая ошибка. Попробуйте позже или обратитесь в поддержку");
            }
        } catch (Exception ex) {
            log.error("Критическая ошибка уведомления", ex);
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

        try {
            Boolean alreadySubscribed = client.isLinkSubscribe(chatId, link)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (Boolean.TRUE.equals(alreadySubscribed)) {
                sendMsg(chatId, "ℹ️ Вы уже подписаны на этот вопрос");
                return;
            }
            Boolean subscriptionResult = client.subscribeLink(chatId, link)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (Boolean.TRUE.equals(subscriptionResult)) {
                sendMsg(chatId, "✅ Вы подписаны на: " + link);
                log.info("Пользователь подписался: chatId={}, questionId={}", chatId, questionIdOp.get());
            } else {
                sendMsg(chatId, "❌ Не удалось подписаться (проверьте ссылку)");
            }

        } catch (Exception e) {
            log.error("Ошибка подписки chatId={}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }
    }


    private void unTrackCommand(long chatId, String linkMsg) throws TelegramApiException {
        String[] parts = linkMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /untrack <ссылка_на_вопрос>");
            return;
        }
        String link = parts[1].trim();

        try {
            Boolean isUnTruck = client.unsubscribeLink(chatId, link)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            if (Boolean.TRUE.equals(isUnTruck)) {
                sendMsg(chatId, "Вы отписались от получение новостей по ссылке: " + link);
                log.info("Пользователь отписался: chatId={}, link={}", chatId, link);
            } else {
                sendMsg(chatId, "❌ Не удалось отписаться (проверьте ссылку)");
            }
        } catch (Exception e) {
            log.error("Ошибка отписки chatId={}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }

    }

    private void getAllSubscribes(long chatId){
       Set<String> links = client.getAllLinks(chatId)
               .timeout(Duration.ofSeconds(5))
               .block();
        if (links == null || links.isEmpty()) {
            sendMsg(chatId, "ℹ️ Вы еще не подписались ни на одну ссылку");
        } else {
            StringBuilder message = new StringBuilder("📋 Ваши подписки:\n");
            int i = 1;
            for (String link : links) {
                message.append(i++).append(" - ").append(link).append("\n");
            }
            sendMsg(chatId, message.toString());
        }
    }

    public void sendMsg(long chatId, String textSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException te) {
            log.error("ERROR / : " + te.getMessage());
        }
    }

    public Mono<Void> sendReactiveMsg(long chatId, String textSend) {
        return Mono.fromRunnable(() -> sendMsg(chatId, textSend))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }


    public Optional<Long> parseQuestionId(String link) {
        try {
            //  для StackOverflow
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

    private boolean isChatRegister(long chatId) {
        return Boolean.TRUE.equals(client.isChatRegister(chatId).timeout(Duration.ofSeconds(5)).block());
    }
}
