package me.ivanmorozov.telegrambot.kafkaTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import me.ivanmorozov.telegrambot.metric.BotMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static me.ivanmorozov.common.kafka.KafkaDataTypeKey.LINK_KEY;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TelegramKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, KafkaRecords.KafkaRequest> kafkaTemplate;
    @Mock
    private BotMetrics metrics;
    @InjectMocks
    private TelegramKafkaProducer kafkaProducer;

    @BeforeEach
    public void setUp() {
        Mockito.when(kafkaTemplate.send(anyString(), anyString(), any(KafkaRecords.KafkaRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    public void sendRequest_sendChatRegister() {
        KafkaRecords.KafkaRequest request = new KafkaRecords.KafkaRequest(1L, MessageTypes.CHAT_REGISTER, Map.of());

        kafkaProducer.sendRequest(1L, request);

        verify(kafkaTemplate).send(eq("scrapper.requests"), eq(String.valueOf(1L)), eq(request));
        verify(metrics).recordKafkaMessageCountRequest(request.type());
    }

    @Test
    public void sendRequest_sendSubscribeLink() {
        KafkaRecords.KafkaRequest request =
                new KafkaRecords.KafkaRequest(1L, MessageTypes.CHAT_REGISTER, Map.of(LINK_KEY, "link.com"));
        kafkaProducer.sendRequest(1L, request);
        verify(kafkaTemplate).send(eq("scrapper.requests"), eq(String.valueOf(1L)), eq(request));
        verify(metrics).recordKafkaMessageCountRequest(request.type());
    }

}
