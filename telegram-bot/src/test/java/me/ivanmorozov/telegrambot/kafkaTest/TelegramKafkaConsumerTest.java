package me.ivanmorozov.telegrambot.kafkaTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaConsumer;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import me.ivanmorozov.telegrambot.metric.BotMetrics;
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
import org.springframework.kafka.test.context.EmbeddedKafka;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.STOCK_KEY;
import static me.ivanmorozov.common.kafka.KafkaTopics.REQUEST_TOPIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramKafkaConsumerTest {

    @Mock
    private RegistrationCache cache;
    @Mock
    private MessageWrapper messageWrapper;
    @Mock
    private BotMetrics botMetrics;
    @InjectMocks
    private TelegramKafkaConsumer consumer;



    @Test
    public void handleResponse_shouldSendUserMessage_whenRegistrationSuccess() {
        when(messageWrapper.sendMessage(anyLong(), anyString(), any()))
                .thenReturn(Mono.empty());
        KafkaRecords.KafkaResponse response = new KafkaRecords.KafkaResponse(1L, MessageTypes.CREATED, Map.of());
        consumer.handleResponse(response);
        verify(messageWrapper).sendMessage(eq(1L), contains("завершена"),any());
    }

    @Test
    public void handleResponse_shouldSendUserMessage_ifTickerInvalid(){
        when(messageWrapper.sendMessage(anyLong(), anyString()))
                .thenReturn(Mono.empty());
        KafkaRecords.KafkaResponse response =
                new KafkaRecords.KafkaResponse(1L, MessageTypes.ACCEPTED, Map.of(STOCK_KEY,false));
        consumer.handleResponse(response);
        verify(messageWrapper).sendMessage(eq(1L), contains("Ошибка"));
    }
    @Test
    public void handleResponse_shouldSendUserMessage_ifTickerValid(){
        when(messageWrapper.sendMessage(eq(1L), anyString())).thenReturn(Mono.empty());
        KafkaRecords.KafkaResponse response =
                new KafkaRecords.KafkaResponse(1L, MessageTypes.ACCEPTED, Map.of(STOCK_KEY,"TEST"));
        consumer.handleResponse(response);
        verify(messageWrapper).sendMessage(eq(1L), contains("подписаны"));
    }

    @Test
    public void handleResponse_shouldSendUserMessage_ifException(){
        when(messageWrapper.sendMessage(eq(1L), anyString())).thenReturn(Mono.empty());
        KafkaRecords.KafkaResponse response = new KafkaRecords.KafkaResponse(1L, MessageTypes.ACCEPTED, Map.of());
        consumer.handleResponse(response);
        verify(messageWrapper).sendMessage(eq(1L), contains("ошибка"));
    }


}
