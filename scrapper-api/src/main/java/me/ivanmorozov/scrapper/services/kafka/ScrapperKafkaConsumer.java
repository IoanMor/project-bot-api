package me.ivanmorozov.scrapper.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.scrapper.services.db.ChatService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperKafkaConsumer {
    private final ChatService chatService;
    private final ScrapperKafkaProducer kafkaProducer;

    @KafkaListener(
            topics = KafkaTopics.TG_CHAT_CREATE,
            groupId = "scrapper-api"
    )
    public void consumeChatRegister(ChatRecords.ChatRegisterRequest request){
        chatService.registerChat(request.chatId());
        log.info("✅ Зарегистрирован chatId: {}", request.chatId());
        kafkaProducer.send(KafkaTopics.TG_CHAT_CREATED,String.valueOf(request.chatId()), new ChatRecords.ChatRegisterRequest(request.chatId()));
    }

    @KafkaListener(
            topics = KafkaTopics.TG_CHAT_EXIST_REQ,
            groupId = "scrapper-api"
    )
    public void consumeChatExist(ChatRecords.ChatExistsRequest request){
        boolean exist = chatService.isChatExist(request.chatId());
        kafkaProducer.send(KafkaTopics.TG_CHAT_EXIST_RES,String.valueOf(request.chatId()),
                new ChatRecords.ChatExistsResponse(request.chatId(), exist));
    }

}
