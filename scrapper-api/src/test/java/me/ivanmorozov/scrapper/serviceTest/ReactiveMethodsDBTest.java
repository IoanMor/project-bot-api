package me.ivanmorozov.scrapper.serviceTest;

import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import me.ivanmorozov.scrapper.services.ReactiveMethodsDB;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;


@SpringBootTest
public class ReactiveMethodsDBTest {

    @Mock
    private TelegramChatRepository chatRepository;

    @InjectMocks
    private ReactiveMethodsDB reactiveMethodsDB;

    @Test
    public void getAllChats_shouldReturnFluxOfChatIds() {
        List<Long> chatIds = List.of(12321L, 232L, 4224L);
        when(chatRepository.getAllChats()).thenReturn(new LinkedHashSet<>(chatIds));

        Flux<Long> result = reactiveMethodsDB.getAllChatsWithRetry();

        StepVerifier.create(result)
                .expectNext(12321L, 232L, 4224L)
                .verifyComplete();
        verify(chatRepository,times(1)).getAllChats();
    }
    @Test
    public void getAllChats_shouldReturnEmptyFlux() {

        when(chatRepository.getAllChats()).thenReturn(Collections.emptySet());

        Flux<Long> result = reactiveMethodsDB.getAllChatsWithRetry();

        StepVerifier.create(result)
                .expectComplete()
                .verify();
        verify(chatRepository,times(1)).getAllChats();
    }

    @Test
    public void getAllChats_shouldRetryOnError(){
        List<Long> chatIds = List.of(1L, 2L);
        Mockito.when(chatRepository.getAllChats())
                .thenThrow(new RuntimeException("DB ERROR"))
                .thenThrow(new RuntimeException("DB ERROR"))
                .thenReturn(new LinkedHashSet<>(chatIds));

        Flux<Long> result = reactiveMethodsDB.getAllChatsWithRetry();

        StepVerifier.create(result)
                .expectNext( 1L,2L)
                .verifyComplete();
        verify(chatRepository,times(3)).getAllChats();
    }


}
