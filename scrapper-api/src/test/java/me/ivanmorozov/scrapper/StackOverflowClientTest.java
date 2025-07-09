package me.ivanmorozov.scrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest
public class StackOverflowClientTest {
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ScrapperMetrics scrapperMetrics;
    @InjectMocks
    private StackOverflowClient stackOverflowClient;


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


}
