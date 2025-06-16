package me.ivanmorozov.scrapper.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.ChatRecords;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.services.db.ChatService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapperKafkaConsumer {
    private final ChatService chatService;
    private final ScrapperKafkaProducer kafkaProducer;


    @KafkaListener(topics = KafkaTopics.REQUEST_TOPIC, groupId = "scrapper-api-group")
    public void handleRequest(KafkaRecords.KafkaRequest request) {
        switch (request.type()) {
            case MessageTypes.CHAT_REGISTER -> {

                if (chatService.isChatExist(request.chatId())) {
                    kafkaProducer.sendResponse(request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.EXIST_CHAT, Map.of()));
                } else {
                    chatService.registerChat(request.chatId());
                    kafkaProducer.sendResponse(
                            request.chatId(),
                            new KafkaRecords.KafkaResponse(request.chatId(), MessageTypes.CREATED, Map.of())
                    );
                }
            }
        }
    }

}
