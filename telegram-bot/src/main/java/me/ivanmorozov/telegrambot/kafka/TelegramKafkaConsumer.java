package me.ivanmorozov.telegrambot.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;

import me.ivanmorozov.common.records.KafkaRecords;

import me.ivanmorozov.telegrambot.service.TelegramBotService;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.ivanmorozov.common.constMsg.Msg.START_TEXT;
import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramKafkaConsumer {
    private final RegistrationCache cache;
    private final TelegramBotService botService;

    @KafkaListener(topics = KafkaTopics.RESPONSE_TOPIC, groupId = "telegram-bot-group")
    public void handleResponse(KafkaRecords.KafkaResponse response) {
        Map<String,Object> dataMap = response.data()!=null? (Map<String, Object>) response.data() : Collections.emptyMap();

        log.info("[-.] Получен ответ: {}", response);
        try {
            switch (response.type()) {
                case MessageTypes.CREATED -> {
                    botService.sendMsg(response.chatId(), "✅ Регистрация завершена! " + START_TEXT);
                }
                case MessageTypes.NOT_CREATED -> {
                    botService.sendMsg(response.chatId(), "⚠️ Не удалось зарегистрировать, попробуйте позже");
                }
                case MessageTypes.EXIST_CHAT_REGISTER -> {
                    botService.sendMsg(response.chatId(), "ℹ️ Вы уже зарегистрированы.");
                }
                case MessageTypes.EXIST_CHAT_CHECK -> {
                    cache.setRegistered(response.chatId(), (boolean)dataMap.get(EXIST_KEY));
                }
                case MessageTypes.EXIST_SUBSCRIBE_LINK -> {
                    botService.sendMsg(response.chatId(), "ℹ️ Вы уже подписсаны на эту ссылку");
                }
                case MessageTypes.SUCCESS -> {
                        String key = dataMap.keySet().iterator().next().toString();
                        String value = dataMap.get(key).toString();
                        botService.sendMsg(response.chatId(), "ℹ️ Вы подписались на " + key + "[" + value + "]");

                }

                case MessageTypes.UNSUBSCRIBE_RESULT_LINK -> {
                        if (dataMap.containsKey(LINK_KEY)) {
                            boolean isSuccess = (boolean) dataMap.get(LINK_KEY);
                            if (isSuccess) {
                                botService.sendMsg(response.chatId(), "✅ Вы успешно отписались от обновлений");
                            } else {
                                botService.sendMsg(response.chatId(), "❌ Не удалось отписаться. Возможно, вы не были подписаны");
                            }
                        }
                }

                case MessageTypes.GET_ALL_LINKS -> {
                        Set<String> links = new HashSet<>();
                        if (dataMap.containsKey(LINK_KEY)) {
                            for (var element : dataMap.values()) {
                                links.add(element.toString());
                            }
                        }
                        if (!links.isEmpty()) {
                            StringBuilder message = new StringBuilder("📋 Ваши подписки:\n");
                            int i = 1;
                            for (String link : links) {
                                message.append(i++).append(" - ").append(link).append("\n");
                            }
                            botService.sendMsg(response.chatId(), message.toString());
                        } else {
                            botService.sendMsg(response.chatId(), "ℹ️ У вас нет активных подписок");
                        }
                }

                case MessageTypes.ACCEPTED -> {
                        try {
                            if (dataMap.get(STOCK_KEY).equals(false)) {
                                botService.sendMsg(response.chatId(), "❌ Ошибка в наименовании тикета(Акции), проверьте правильность");
                                return;
                            } else {
                                String ticker = (String) dataMap.get(STOCK_KEY);
                                botService.sendMsg(response.chatId(), "✅ Вы подписаны на: " + ticker);
                            }
                        } catch (Exception e) {
                            botService.sendMsg(response.chatId(), "⚠️ Произошла непредвиденная ошибка");
                        }
                }
                case MessageTypes.EXIST_SUBSCRIBE_STOCK -> {
                    botService.sendMsg(response.chatId(), "ℹ️ Вы уже подписаны на этот тикер(Акцию)");
                }
                case MessageTypes.GET_ALL_STOCK -> {
                        StringBuilder message = new StringBuilder("📋 Стоимость интерисующих вас акций:\n");
                        int counter = 1;
                        for (var entry : dataMap.entrySet()) {
                            String stock = (String) entry.getKey();
                            Double price = (Double) entry.getValue();
                            message.append(counter++)
                                    .append(" - ")
                                    .append(stock)
                                    .append(" - rub.")
                                    .append(price != null ? String.format("%.2f", price) : "N/A")
                                    .append("\n");
                        }
                        botService.sendMsg(response.chatId(), message.toString());
                }
                case MessageTypes.UNSUBSCRIBE_RESULT_STOCK -> {
                        if (dataMap.containsKey(STOCK_KEY)) {
                            boolean isSuccess = (boolean) dataMap.get(STOCK_KEY);
                            if (isSuccess) {
                                botService.sendMsg(response.chatId(), "✅ Вы успешно отписались от тикера(Акции)");
                            } else {
                                botService.sendMsg(response.chatId(), "❌ Не удалось отписаться. Возможно, вы не были подписаны на данный тикер(Акцию)");
                            }
                        }
                }
                case MessageTypes.STOCK_SHEDULED_MSG -> {
                        botService.sendMsg(response.chatId(), dataMap.get(STOCK_KEY).toString());
                }
                default -> {
                    log.warn("Неизвестный тип: {}", response.type());
                    botService.sendMsg(response.chatId(), "⚠️ Произошла непредвиденная ошибка");
                }
            }
        } catch (Exception e) {
            log.error("[.] Ошибка KafkaListener. Тип сообщения: {}, chatId: {}, данные: {}, ошибка: {}",
                    response.type(), response.chatId(), response.data(), e.getMessage(), e);
            botService.sendMsg(response.chatId(), "⚠️ Произошла ошибка. Попробуйте позже.");
            throw new IllegalArgumentException("Неизвестный тип: " + response.type());
        }
    }

}
