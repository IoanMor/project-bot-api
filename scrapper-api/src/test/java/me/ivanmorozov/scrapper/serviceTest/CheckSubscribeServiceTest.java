package me.ivanmorozov.scrapper.serviceTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import me.ivanmorozov.scrapper.services.ReactiveMethodsDB;
import net.bytebuddy.asm.MemberSubstitution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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
        when(linkRepository.getLinks(1L)).thenReturn(subscribes);
        when(client.trackLink(123L, 1L, "stackoverflow.com/questions/123"))
                .thenReturn(Mono.just(true));

       Mono<Void> result = checkSubscribeService.checkUserSubscriptions(1L);

        StepVerifier.create(result)
                        .verifyComplete();
        verify(client, times(1)).trackLink(123L, 1L, "stackoverflow.com/questions/123");
        ArgumentCaptor<KafkaRecords.KafkaResponse> captor = ArgumentCaptor.forClass(KafkaRecords.KafkaResponse.class);
        verify(kafkaProducer, times(1)).sendResponse(eq(1L), captor.capture());

        KafkaRecords.KafkaResponse response = captor.getValue();
        assertEquals(1L,response.chatId());
        assertEquals(MessageTypes.STOCK_SHEDULED_MSG,response.type());


    }

}
