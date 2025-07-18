package me.ivanmorozov.scrapper.serviceTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.repositories.StockRepository;
import me.ivanmorozov.scrapper.services.CheckPriceStockService;
import me.ivanmorozov.scrapper.services.ReactiveMethodsDB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckPriceStockServiceTest {
    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockApiClient stockApiClient;
    @Mock
    private ScrapperKafkaProducer kafkaProducer;
    @Mock
    private ReactiveMethodsDB reactiveMethod;

    @InjectMocks
    private CheckPriceStockService checkPriceStockService;

    @Test
    public void checkSubscribe_shouldCallKafka(){
       when(stockRepository.getTickers(1L)).thenReturn(Set.of("TEST"));
       when(stockApiClient.getPrice("TEST")).thenReturn(Mono.just(BigDecimal.valueOf(100)));

       Mono<Void> result = checkPriceStockService.checkSubscribeStock(1L);

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<KafkaRecords.KafkaResponse> captor = ArgumentCaptor.forClass(KafkaRecords.KafkaResponse.class);

        verify(kafkaProducer,times(1)).sendResponse(eq(1L),captor.capture());

        KafkaRecords.KafkaResponse response = captor.getValue();
        assertEquals(1L,response.chatId());
        assertEquals(MessageTypes.STOCK_SHEDULED_MSG, response.type());
    }


    @Test
    public void checkSubscribe_shouldCallExceptionIfTrackIsInvalid(){
        when(stockRepository.getTickers(1L)).thenReturn(Set.of("TEST"));
        when(stockApiClient.getPrice("TEST")).thenReturn(null);

        Mono<Void> result = checkPriceStockService.checkSubscribeStock(1L);
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class).verify();
    }

}
