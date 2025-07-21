package me.ivanmorozov.scrapper.kafkaTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@ExtendWith(MockitoExtension.class)
public class ScrapperKafkaProducerTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private ScrapperMetrics metrics;

    @InjectMocks
    ScrapperKafkaProducer scrapperKafkaProducer;

    @Test
    void sendResponse_shouldSendKafkaMessage() {

        KafkaRecords.KafkaResponse response = new KafkaRecords.KafkaResponse(1L, MessageTypes.ACCEPTED, Map.of("STOCK_KEY","TEST"));

        Mockito.when(kafkaTemplate.send(anyString(),anyString(),any(KafkaRecords.KafkaResponse.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        scrapperKafkaProducer.sendResponse(1L,response);

        verify(kafkaTemplate).send(eq("scrapper.responses"), eq(String.valueOf(1L)), eq(response));
        verify(metrics).recordKafkaMessageCountResponse(response.type());

    }

}
