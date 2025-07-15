package me.ivanmorozov.scrapper.serviceTest;

import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import me.ivanmorozov.scrapper.services.ReactiveMethodsDB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CheckSubscribeServiceTest {
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private StackOverflowClient client;
    @Mock
    private ScrapperKafkaProducer kafkaProducer;
    @Mock
    private ReactiveMethodsDB reactiveMethod;

    @InjectMocks
    private CheckSubscribeService checkSubscribeService;

    @Test
    public void checkUserSubscriptions_shouldCallKafka() {
        Set<String> subscribes = Set.of("stackoverflow.com/questions/123");
        Mockito.when(linkRepository.getLinks(1L)).thenReturn(subscribes);

        verify(client, times(1)).trackLink(123L, 1L, "stackoverflow.com/questions/123");
        verify(kafkaProducer, times(1)).sendResponse(1L, new KafkaRecords.KafkaResponse(1L, null, null));
    }

}
