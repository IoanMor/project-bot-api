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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
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

    @Spy
    @InjectMocks
    private CheckSubscribeService checkSubscribeService;

    @Test
    public void checkUserSubscriptions_shouldCallKafka() {

        when(linkRepository.getLinks(1L)).thenReturn(Set.of("stackoverflow.com/questions/123"));
        when(client.trackLink(anyLong(), anyLong(), anyString()))
                .thenReturn(Mono.just(true));

        Mono<Void> result = checkSubscribeService.checkUserSubscriptions(1L);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<KafkaRecords.KafkaResponse> captor = ArgumentCaptor.forClass(KafkaRecords.KafkaResponse.class);
        verify(kafkaProducer, times(1)).sendResponse(eq(1L), captor.capture());

        KafkaRecords.KafkaResponse response = captor.getValue();
        assertEquals(1L, response.chatId());
        assertEquals(MessageTypes.STOCK_SHEDULED_MSG, response.type());


    }

    @Test
    public void checkUserSubscriptions_shouldCallExceptionIfInvalidLink() {
        Set<String> subscribes = Set.of("invalidLink.com");
        when(linkRepository.getLinks(1L)).thenReturn(subscribes);
        Mono<Void> result = checkSubscribeService.checkUserSubscriptions(1L);
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class).verify();
    }
    @Test
    public void checkUserSubscriptions_shouldDontCallKafkaIfTrackLinkReturnFalse() {
        when(linkRepository.getLinks(1L)).thenReturn(Set.of("stackoverflow.com/questions/123"));
        when(client.trackLink(anyLong(), anyLong(), anyString()))
                .thenReturn(Mono.just(false));

        Mono<Void> result = checkSubscribeService.checkUserSubscriptions(1L);

        StepVerifier.create(result)
                .verifyComplete();
        verify(kafkaProducer,never()).sendResponse(anyLong(),any());
    }

    @Test
    public void checkUpdates_shouldHandleAllChats(){
        when(reactiveMethod.getAllChatsWithRetry()).thenReturn(Flux.just(1L,2L));
        when(checkSubscribeService.checkUserSubscriptions(anyLong())).thenReturn(Mono.empty());

        checkSubscribeService.checkUpdates();

        verify(checkSubscribeService).checkUserSubscriptions(1L);
        verify(checkSubscribeService).checkUserSubscriptions(2L);
    }

    @Test
    public void checkUpdates_shouldHandleException(){
        when(reactiveMethod.getAllChatsWithRetry()).thenReturn(Flux.just(1L,2L));
        when(checkSubscribeService.checkUserSubscriptions(anyLong())).thenReturn(Mono.error(new RuntimeException("err")));

        checkSubscribeService.checkUpdates();

    }
}
