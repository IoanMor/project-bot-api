package me.ivanmorozov.telegrambot.clientTest;

import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.telegrambot.client.MessageTelegramClient;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import me.ivanmorozov.telegrambot.config.WebClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MessageTelegramClientTest {
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private TelegramBotConfig telegramBotConfig;

    private MessageTelegramClient telegramClient;


    @BeforeEach
    void setUp() {
        telegramClient = new MessageTelegramClient(webClient,telegramBotConfig);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    }

    @Test
    public void sendMessageClient_shouldSendRequest(){
        Mono<Void> result = telegramClient.sendMessageClient(1L,"text");
        verify(webClient).post();
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void sendMessageClient_shouldHandleExceptionAndGetLogWithRetry(){
        when(responseSpec.bodyToMono(Void.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        Mono<Void> result = telegramClient.sendMessageClient(1L,"text");

        StepVerifier.create(result).expectError(RuntimeException.class).verify();

    }

}
