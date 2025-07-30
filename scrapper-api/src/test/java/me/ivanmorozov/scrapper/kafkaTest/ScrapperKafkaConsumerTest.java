package me.ivanmorozov.scrapper.kafkaTest;

import me.ivanmorozov.common.kafka.KafkaTopics;
import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaConsumer;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.repositories.StockRepository;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import org.hibernate.annotations.Check;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;
import java.util.Map;


import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.LINK_KEY;
import static me.ivanmorozov.common.kafka.KafkaTopics.REQUEST_TOPIC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {REQUEST_TOPIC})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScrapperKafkaConsumerTest {

    @Autowired
    private TelegramChatRepository chatRepository;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private StockRepository stockRepository;
    @MockBean
    private ScrapperKafkaProducer kafkaProducer;
    @Autowired
    private StockApiClient stockApiClient;
    @Autowired
    private ScrapperKafkaConsumer scrapperKafkaConsumer;
    @Autowired
    private ScrapperKafkaConsumer consumer;

    private final long chatId = 1L;
    private final String link = "stackoverflow.com/questions/123";

    @BeforeEach
    @AfterEach
    void setup() {
        chatRepository.deleteTelegramChatByChatId(chatId);
        linkRepository.removeLink(chatId,link);
    }

    @Test
    public void handleRequest_shouldRegisterChat_whenChatNotExists() {

        KafkaRecords.KafkaRequest request = new KafkaRecords.KafkaRequest(chatId, MessageTypes.CHAT_REGISTER, Map.of());

        consumer.handleRequest(request);
        Long found = chatRepository.existChat(chatId);
        assertNotNull(found, "Чат должен быть зарегистрирован");
        assertEquals(chatId, found);
        verify(kafkaProducer,times(1))
                .sendResponse(chatId, new KafkaRecords.KafkaResponse((chatId),MessageTypes.CREATED,Map.of()));
    }

    @Test
    public void handleRequest_shouldSendKafkaMessage_whenChatExists() {

        chatRepository.registrationChat(chatId, LocalDateTime.now());
        KafkaRecords.KafkaRequest request = new KafkaRecords.KafkaRequest(chatId, MessageTypes.CHAT_REGISTER, Map.of());

        consumer.handleRequest(request);

        Long found = chatRepository.existChat(chatId);
        assertNotNull(found, "Чат должен быть зарегистрирован");
        assertEquals(chatId, found);

        verify(kafkaProducer,times(1))
                .sendResponse(chatId, new KafkaRecords.KafkaResponse((chatId),MessageTypes.EXIST_CHAT_REGISTER,Map.of()));
    }

    @Test
    public void handleRequest_shouldSubscribeLink_whenLinkNotExist(){
        chatRepository.registrationChat(chatId, LocalDateTime.now());

        KafkaRecords.KafkaRequest request = new KafkaRecords.KafkaRequest(chatId, MessageTypes.LINK_SUBSCRIBE, Map.of(LINK_KEY,link));
        consumer.handleRequest(request);

        boolean found = linkRepository.existsLink(chatId,link);

        verify(kafkaProducer,times(1))
                .sendResponse(chatId,new KafkaRecords.KafkaResponse((chatId),MessageTypes.SUCCESS,Map.of(LINK_KEY,link)));

        assertTrue(found);
    }

    @Test
    public void handleRequest_shouldDontSubscribeLink_whenLinkExist(){
        chatRepository.registrationChat(chatId, LocalDateTime.now());
        linkRepository.subscribeLink(chatId,link);

        boolean found = linkRepository.existsLink(chatId,link);
        assertTrue(found);

        KafkaRecords.KafkaRequest request = new KafkaRecords.KafkaRequest(chatId, MessageTypes.LINK_SUBSCRIBE, Map.of(LINK_KEY,link));
        consumer.handleRequest(request);

        verify(kafkaProducer,times(1))
                .sendResponse(chatId,new KafkaRecords.KafkaResponse((chatId),MessageTypes.EXIST_SUBSCRIBE_LINK,Map.of()));

    }
}
