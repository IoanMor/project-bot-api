package me.ivanmorozov.scrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.scrapper.config.WebClientConfig;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import org.apache.kafka.common.requests.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class StackOverflowClientTest {
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ScrapperMetrics scrapperMetrics;
    @Mock
    private WebClientConfig webClientConfig;

    private StackOverflowClient stackOverflowClient;

    @BeforeEach
    void setUp() {
        stackOverflowClient = new StackOverflowClient(webClient, linkRepository, scrapperMetrics, webClientConfig);
    }


    @Test
    public void trackLink_shouldCheckCountAnswerAndCompareWithDB() {
        JsonNode fakeResponse = new ObjectMapper().createObjectNode().put("total", 5);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeResponse));
        when(linkRepository.getCountAnswer(1L, "https://stackoverflow.com/q/123")).thenReturn(5);

        Mono<Boolean> result = stackOverflowClient.trackLink(123L, 1L, "https://stackoverflow.com/q/123");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        verify(linkRepository, never()).updateCountAnswer(anyLong(), anyString(), anyInt());
        verify(scrapperMetrics).recordApiCallSuccess("stackoverflow-API");

    }

    @Test
    public void trackLink_updatesAnswerCount_ifNewCountIsGreater(){
        JsonNode fakeResponse = new ObjectMapper().createObjectNode().put("total", 1);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeResponse));
        when(linkRepository.getCountAnswer(1L, "https://stackoverflow.com/q/123")).thenReturn(2);

        Mono<Boolean> result = stackOverflowClient.trackLink(123L, 1L, "https://stackoverflow.com/q/123");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        verify(linkRepository, atLeast(1)).updateCountAnswer(anyLong(), anyString(), anyInt());
        verify(scrapperMetrics).recordApiCallSuccess("stackoverflow-API");
    }
    @Test
    public void trackLink_updatesAnswerCount_ifCountIsLess(){
        JsonNode fakeResponse = new ObjectMapper().createObjectNode().put("total", 1);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(fakeResponse));
        when(linkRepository.getCountAnswer(1L, "https://stackoverflow.com/q/123")).thenReturn(2);

        Mono<Boolean> result = stackOverflowClient.trackLink(123L, 1L, "https://stackoverflow.com/q/123");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        verify(linkRepository, never()).updateCountAnswer(anyLong(), anyString(), anyInt());
        verify(scrapperMetrics).recordApiCallSuccess("stackoverflow-API");
    }

    @Test
    public void trackLink_shouldReturnFalse_ifApiGetError(){

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(404, "Ошибка API-NOT FOUND", HttpHeaders.EMPTY, null, null)));

        Mono<Boolean> result = stackOverflowClient.trackLink(123L, 1L, "https://stackoverflow.com/q/123");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(linkRepository, never()).updateCountAnswer(anyLong(), anyString(), anyInt());
        verify(scrapperMetrics).recordApiCallFailure(argThat(s->s.contains("Ошибка API-NOT FOUND")));
    }

}
