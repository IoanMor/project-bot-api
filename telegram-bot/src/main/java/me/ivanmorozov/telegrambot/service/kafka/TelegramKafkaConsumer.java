package me.ivanmorozov.telegrambot.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.records.ChatRecords;

import me.ivanmorozov.common.records.LinkRecords;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static me.ivanmorozov.common.constMsg.Msg.START_TEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramKafkaConsumer {
    private final TelegramBotService botService;
    private final TelegramKafkaProducer kafkaProducer;

    @KafkaListener(topics = KafkaTopics.TG_CHAT_EXIST_RES, groupId = "telegram-bot")
    public void consumeChatExistsResponse(ChatRecords.ChatExistsResponse response) {
        if (response.isExist()) {
            botService.sendMsg(response.chatId(), "ℹ️ Вы уже зарегистрированы.");
        } else {
            botService.sendMsg(response.chatId(), "ℹ️ Регистрация начата...");
            kafkaProducer.send(KafkaTopics.TG_CHAT_CREATE, String.valueOf(response.chatId()), new ChatRecords.ChatRegisterRequest(response.chatId()));
        }
    }

    @KafkaListener(topics = KafkaTopics.TG_CHAT_CREATED)
    public void handleChatCreated(ChatRecords.ChatRegisterRequest request) {
        botService.sendMsg(request.chatId(),
                "✅ Регистрация завершена! " + START_TEXT
        );
    }

    @KafkaListener(topics = KafkaTopics.LINK_SUBSCRIBE_EXIST_RES, groupId = "telegram-bot" )
    public void consumerLinkExistResponse(LinkRecords.LinkExistResponse response){
        if (response.isExist()){
            botService.sendMsg(response.chatId(), "ℹ️ Вы уже подписаны на этот вопрос");
        } else {
            kafkaProducer.send(KafkaTopics.LINK_SUBSCRIBE, response.link(), new LinkRecords.LinkSubscribeRequest(response.chatId(), response.link()));
        }
    }
    @KafkaListener(topics = KafkaTopics.LINK_SUBSCRIBE)
    public void handleLinkSubscribe(LinkRecords.LinkSubscribeRequest request) {
       botService.sendMsg(request.chatId(), "✅ Вы подписаны на: " + request.link());
    }
}
