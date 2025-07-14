package me.ivanmorozov.scrapper.clientTest;

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
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    public void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(),any())).thenReturn(responseSpec);

        stockApiClient = new StockApiClient(webClient, scrapperMetrics);

    }

    @Test
    public void getPrice_shouldReturnPriceIfTickerValid() {
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data", new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                        .add("TEST")
                                        .add("...")
                                        .add("...")
                                        .add(1200)
                                )
                        )
                );

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));
        Mono<BigDecimal> result = stockApiClient.getPrice("TEST");
        StepVerifier.create(result)
                .expectNext(BigDecimal.valueOf(1200))
                .verifyComplete();
        verify(scrapperMetrics).recordApiCallSuccess("moex-getPrice-API");
    }

    @Test
    public void getPrice_shouldHandleIncorrectJson() {
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode());

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));

        Mono<BigDecimal> result = stockApiClient.getPrice("TEST");

        StepVerifier.create(result)
                .expectError(RuntimeException.class).verify();

    }

    @Test
    public void getPrice_shouldHandleApiError() {
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(404, "Ошибка API-NOT FOUND", HttpHeaders.EMPTY, null, null))
                );
        Mono<BigDecimal> result = stockApiClient.getPrice("TEST");
        StepVerifier.create(result)
                .expectError(RuntimeException.class).verify();

    }
    @Test
    public void getTicker_shouldReturnNameTickerIfTickerValid() {
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data", new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                        .add("TEST")
                                        .add("...")
                                        .add("...")
                                        .add(1200)
                                )
                        )
                );

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));
        Mono<String> result = stockApiClient.getTicker("TEST");
        StepVerifier.create(result)
                .expectNext("TEST")
                .verifyComplete();
        verify(scrapperMetrics).recordApiCallSuccess("moex-getTicker-API");
    }

    @Test
    public void getTicker_shouldHandleNameTickerIsBlank(){
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data", new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                        .add(" ")
                                        .add("...")
                                        .add("...")
                                )
                        )
                );
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));
        Mono<String> result = stockApiClient.getTicker("TEST");
        StepVerifier.create(result)
                .expectError(RuntimeException.class).verify();
    }

    @Test
    public void getTicker_shouldHandleApiError() {
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(404, "Ошибка API-NOT FOUND", HttpHeaders.EMPTY, null, null))
                );
        Mono<String> result = stockApiClient.getTicker("TEST");
        StepVerifier.create(result)
                .expectError(RuntimeException.class).verify();

    }

    @Test
    public void validTickerName_shouldReturnTrueIfValid(){
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data", new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                        .add("TEST")
                                        .add("...")
                                        .add("...")
                                        .add(1200)
                                )
                        )
                );

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));
        Mono<Boolean> result = stockApiClient.validateTickerName("TEST");
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    public void validTickerName_shouldReturnFalseIfNoValid(){
        ObjectNode fakeNode = new ObjectMapper().createObjectNode()
                .set("securities", new ObjectMapper().createObjectNode()
                        .set("data", new ObjectMapper().createArrayNode()
                                .add(new ObjectMapper().createArrayNode()
                                        .add("TEST")
                                        .add("...")
                                        .add("...")
                                        .add(1200)
                                )
                        )
                );

        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeNode));
        Mono<Boolean> result = stockApiClient.validateTickerName("TEST123");
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

    }


}
