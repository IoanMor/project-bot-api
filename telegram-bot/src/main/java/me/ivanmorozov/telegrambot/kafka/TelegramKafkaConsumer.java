package me.ivanmorozov.telegrambot.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;

import me.ivanmorozov.common.records.KafkaRecords;


import me.ivanmorozov.telegrambot.client.MessageTelegramClient;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

import static me.ivanmorozov.common.constMsg.Msg.START_TEXT;
import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramKafkaConsumer {
    private final RegistrationCache cache;
    private final MessageTelegramClient sendMessage;

    @KafkaListener(topics = KafkaTopics.RESPONSE_TOPIC, groupId = "telegram-bot-group")
    public void handleResponse(KafkaRecords.KafkaResponse response) {
        Map<String, Object> dataMap = response.data() != null ? (Map<String, Object>) response.data() : Collections.emptyMap();

        log.info("[-.] Получен ответ: {}", response);
        try {
            switch (response.type()) {
                case MessageTypes.CREATED -> {
                    sendMessage.sendMessageClient(response.chatId(), "✅ Регистрация завершена! " + START_TEXT).subscribe();
                }
                case MessageTypes.NOT_CREATED -> {
                    sendMessage.sendMessageClient(response.chatId(), "⚠️ Не удалось зарегистрировать, попробуйте позже").subscribe();
                }
                case MessageTypes.EXIST_CHAT_REGISTER -> {
                    sendMessage.sendMessageClient(response.chatId(), "ℹ️ Вы уже зарегистрированы.").subscribe();
                }
                case MessageTypes.EXIST_CHAT_CHECK -> {
                    cache.setRegistered(response.chatId(), (boolean) dataMap.get(EXIST_KEY));
                }
                case MessageTypes.EXIST_SUBSCRIBE_LINK -> {
                    sendMessage.sendMessageClient(response.chatId(), "ℹ️ Вы уже подписсаны на эту ссылку").subscribe();
                }
                case MessageTypes.SUCCESS -> {
                    String key = dataMap.keySet().iterator().next().toString();
                    String value = dataMap.get(key).toString();
                    sendMessage.sendMessageClient(response.chatId(), "ℹ️ Вы подписались на " + key + "[" + value + "]").subscribe();

                }

                case MessageTypes.UNSUBSCRIBE_RESULT_LINK -> {
                    if (dataMap.containsKey(LINK_KEY)) {
                        boolean isSuccess = (boolean) dataMap.get(LINK_KEY);
                        if (isSuccess) {
                            sendMessage.sendMessageClient(response.chatId(), "✅ Вы успешно отписались от обновлений").subscribe();
                        } else {
                            sendMessage.sendMessageClient(response.chatId(), "❌ Не удалось отписаться. Возможно, вы не были подписаны").subscribe();
                        }
                    }
                }

                case MessageTypes.GET_ALL_LINKS -> {
                    Set<String> links = new HashSet<>();
                    Object value = dataMap.get(LINK_KEY);
                    if (value instanceof Collection<?>) {
                        for (Object obj : (Collection<?>) value) {
                            links.add(obj.toString());
                        }
                    }
                    if (!links.isEmpty()) {
                        StringBuilder message = new StringBuilder("📋 Ваши подписки:\n");
                        int i = 1;
                        for (String link : links) {
                            message.append(i++).append(" - ").append(link).append("\n");
                        }
                        sendMessage.sendMessageClient(response.chatId(), message.toString()).subscribe();
                    } else {
                        sendMessage.sendMessageClient(response.chatId(), "ℹ️ У вас нет активных подписок").subscribe();
                    }
                }

                case MessageTypes.ACCEPTED -> {
                    try {
                        if (dataMap.get(STOCK_KEY).equals(false)) {
                            sendMessage.sendMessageClient(response.chatId(), "❌ Ошибка в наименовании тикета(Акции), проверьте правильность").subscribe();
                            return;
                        } else {
                            String ticker = (String) dataMap.get(STOCK_KEY);
                            sendMessage.sendMessageClient(response.chatId(), "✅ Вы подписаны на: " + ticker).subscribe();
                        }
                    } catch (Exception e) {
                        sendMessage.sendMessageClient(response.chatId(), "⚠️ Произошла непредвиденная ошибка").subscribe();
                    }
                }
                case MessageTypes.EXIST_SUBSCRIBE_STOCK -> {
                    sendMessage.sendMessageClient(response.chatId(), "ℹ️ Вы уже подписаны на этот тикер(Акцию)").subscribe();
                }
                case MessageTypes.GET_ALL_STOCK -> {
                    StringBuilder message = new StringBuilder("📋 Стоимость интерисующих вас акций:\n");
                    int counter = 1;
                    for (var entry : dataMap.entrySet()) {
                        String stock = (String) entry.getKey();
                        Double price = ((Number) entry.getValue()).doubleValue();
                        message.append(counter++)
                                .append(" - ")
                                .append(stock)
                                .append(" - rub.")
                                .append(price != null ? String.format("%.2f", price) : "N/A")
                                .append("\n");
                    }
                    sendMessage.sendMessageClient(response.chatId(), message.toString()).subscribe();
                }
                case MessageTypes.UNSUBSCRIBE_RESULT_STOCK -> {
                    if (dataMap.containsKey(STOCK_KEY)) {
                        boolean isSuccess = (boolean) dataMap.get(STOCK_KEY);
                        if (isSuccess) {
                            sendMessage.sendMessageClient(response.chatId(), "✅ Вы успешно отписались от тикера(Акции)").subscribe();
                        } else {
                            sendMessage.sendMessageClient(response.chatId(), "❌ Не удалось отписаться. Возможно, вы не были подписаны на данный тикер(Акцию)").subscribe();
                        }
                    }
                }
                case MessageTypes.STOCK_SHEDULED_MSG -> {
                    sendMessage.sendMessageClient(response.chatId(), dataMap.get(STOCK_KEY).toString()).subscribe();
                }
                default -> {
                    log.warn("Неизвестный тип: {}", response.type());
                    sendMessage.sendMessageClient(response.chatId(), "⚠️ Произошла непредвиденная ошибка").subscribe();
                }
            }
        } catch (Exception e) {
            log.error("[.] Ошибка KafkaListener. Тип сообщения: {}, chatId: {}, данные: {}, ошибка: {}",
                    response.type(), response.chatId(), response.data(), e.getMessage(), e);
            sendMessage.sendMessageClient(response.chatId(), "⚠️ Произошла ошибка. Попробуйте позже.").subscribe();
            throw new IllegalArgumentException("Неизвестный тип: " + response.type());
        }
    }

}
