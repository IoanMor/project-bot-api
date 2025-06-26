package me.ivanmorozov.telegrambot.service;


import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import me.ivanmorozov.telegrambot.service.kafka.TelegramKafkaProducer;
import me.ivanmorozov.telegrambot.util.RegistrationCache;
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
import static me.ivanmorozov.common.linkUtil.LinkUtilStackOverFlow.parseQuestionId;


import java.util.*;



@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramBotConfig botConfig;
    private final RegistrationCache cache;
    private final TelegramKafkaProducer kafkaProducer;


    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public TelegramBotService(TelegramBotConfig botConfig, RegistrationCache cache, TelegramKafkaProducer kafkaProducer) {
        this.botConfig = botConfig;
        this.cache = cache;
        this.kafkaProducer = kafkaProducer;


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
                startCommand(chatId, userName);
                return;
            }

            if (!isChatRegister(chatId)) {
                sendMsg(chatId, "⛔ Для использования бота необходимо зарегистрироваться!\nВведите команду /start");
                return;
            }

            if (msg.startsWith("/track")) {
                handleTrackCommand(msg, chatId);
            } else if (msg.startsWith("/untrack")) {
                unTrackCommand(chatId, msg);
            } else if (msg.startsWith("/tstock")) {
                trackStock(chatId, msg);
            } else if (msg.startsWith("/utstock")) {
                untrackStock(chatId, msg);
            } else if (msg.startsWith("/stock")) {
                getAllStockSubscribes(chatId);
            } else if (msg.startsWith("/links")) {
                getAllLinksSubscribes(chatId);
            } else if ("/help".equalsIgnoreCase(msg)) {
                sendMsg(chatId, HELP_TEXT);
            } else {
                sendMsg(chatId, "❌ Неизвестная команда. Введите /help для списка команд");
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


    private void startCommand(long chatId, String userName)  {
        try {
            String safeName = userName != null ? userName : "пользователь";
            sendMsg(chatId, "Приветствую, " + safeName + "...");
            kafkaProducer.sendChatRegisterRequest(chatId);
            sendMsg(chatId, "🔍 Проверяем вашу регистрацию...");
        } catch (Exception e) {
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
            log.error("Произошла ошибка в классе TelegramBotService метода startcommand, [{}]", e.getMessage());
        }

    }


    private void handleTrackCommand(String linkMsg, long chatId)  {

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
            kafkaProducer.sendSubscribeLinkRequest(chatId, link);
        } catch (Exception e) {
            log.error("Ошибка подписки chatId={}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }
    }


    private void unTrackCommand(long chatId, String linkMsg)  {
        String[] parts = linkMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /untrack <ссылка_на_вопрос>");
            return;
        }
        String link = parts[1].trim();
        try {
            kafkaProducer.sendUnSubscribeLinkRequest(chatId, link);
            sendMsg(chatId, "⌛ Идёт отписка...");
        } catch (Exception e) {
            log.error("Ошибка отписки chatId={}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }
    }

    private void getAllLinksSubscribes(long chatId) {
        try {
            kafkaProducer.sendAllSubscribeLinksRequest(chatId);
        } catch (Exception e) {
            log.error("Произошла ошибка в классе TelegramBotService метода getAllLinksSubscribes, [{}]", e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }
    }

    public void trackStock(long chatId, String userMsg) {
        String[] parts = userMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /tSock <тикер_акции>");
            return;
        }
        String ticker = parts[1].trim();

        if (ticker.isBlank() || ticker == null) {
            sendMsg(chatId, "❌ Неверный формат. Пример: /tStock SBER (пример акции Сбербанк)");
            return;
        }
        try {
           kafkaProducer.sendSubscribeStockRequest(chatId,ticker);
        } catch (Exception e) {
            log.error("Ошибка при подписке на акцию {}|{}", ticker, e.getMessage());
            sendMsg(chatId, "⚠️ При подписке что-то пошло не так");
        }
    }

    public void untrackStock(long chatId, String userMsg) {
        String[] parts = userMsg.split("\\s+", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "ℹ️ Использование: /utstock <тикер_акции>");
            return;
        }
        String ticker = parts[1].trim();

        try {
          kafkaProducer.sendUnSubscribeStockRequest(chatId,ticker);
        } catch (Exception e) {
            log.error("Ошибка отписки от акции chatId={}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Временная ошибка сервера");
        }

    }

    public void getAllStockSubscribes(long chatId) {
        try {
           kafkaProducer.sendGetStockSubscribeRequest(chatId);
        } catch (Exception e) {
            log.error("Ошибка получения подписок для chatId {}: {}", chatId, e.getMessage());
            sendMsg(chatId, "⚠️ Произошла ошибка при получении данных. Попробуйте позже.");
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

    public boolean isChatRegister(long chatId) throws TelegramApiException {

        Boolean cachedStatus = cache.isRegistered(chatId);
        if (cachedStatus != null) {
            return cachedStatus;
        }


        kafkaProducer.sendIsChatRegisterRequest(chatId);


        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) {
            try {
                Thread.sleep(200);
                Boolean status = cache.isRegistered(chatId);
                if (status != null) {
                    return status;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TelegramApiException("Ожидание прервано");
            }
        }
        sendMsg(chatId, "⌛ Сервис временно недоступен, попробуйте позже");
        return false;
    }


}
