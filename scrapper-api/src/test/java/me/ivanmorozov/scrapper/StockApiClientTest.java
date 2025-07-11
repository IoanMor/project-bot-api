package me.ivanmorozov.scrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.ivanmorozov.scrapper.client.StockApiClient;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class StockApiClientTest {
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private ScrapperMetrics scrapperMetrics;
    private StockApiClient stockApiClient;

    @BeforeEach
    public void setUp(){
        stockApiClient = new StockApiClient(webClient,scrapperMetrics);
    }

    @Test
    public void getPrice_shouldReturnPriceIfTickerValid(){
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data",new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                .add("LKOH")
                                .add("...")
                                .add("...")
                                .add(1200)
                                )
                        )

                );

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));

        Mono<BigDecimal> result = stockApiClient.getPrice("LKOH");

        StepVerifier.create(result)
                .expectNext(BigDecimal.valueOf(1200))
                .verifyComplete();

        verify(scrapperMetrics).recordApiCallSuccess("moex-getPrice-API");


    }


}
