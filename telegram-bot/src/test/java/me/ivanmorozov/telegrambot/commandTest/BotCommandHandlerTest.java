package me.ivanmorozov.telegrambot.commandTest;

import io.micrometer.core.instrument.MeterRegistry;
import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.core.command.GetAllLinksSubscribeCommand;
import me.ivanmorozov.telegrambot.core.command.StartCommand;
import me.ivanmorozov.telegrambot.core.command.TrackLinkCommand;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BotCommandHandlerTest {
    @Mock
    private TelegramKafkaProducer kafkaProducer;
    @Mock
    private MessageWrapper messageWrapper;
    @Mock
    private MeterRegistry meterRegistry;
    @InjectMocks
    private StartCommand startCommand;
    @InjectMocks
    private TrackLinkCommand trackLinkCommand;
    @InjectMocks
    private GetAllLinksSubscribeCommand getAllLinks;

    private final long chatId = 1L;

    @Test
    public void execute_startCommand() {
        when(messageWrapper.sendMessage(eq(chatId), anyString(), any())).thenReturn(Mono.empty());
        when(messageWrapper.sendMessage(eq(chatId), anyString())).thenReturn(Mono.empty());
        startCommand.execute(chatId, "Name", new String[]{});
        verify(kafkaProducer, Mockito.times(1)).sendChatRegisterRequest(eq(chatId));
    }

    @Test
    public void execute_startCommand_kafkaThrowsException() {
        when(messageWrapper.sendMessage(eq(chatId), anyString(), any())).thenReturn(Mono.empty());
        when(messageWrapper.sendMessage(eq(chatId), anyString())).thenReturn(Mono.empty());
        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaProducer).sendChatRegisterRequest(eq(chatId));
        assertThrows(RuntimeException.class, () -> {
            startCommand.execute(chatId, "Name", new String[]{});
        });
        verify(messageWrapper).sendMessage(eq(chatId), eq("⚠️ Временная ошибка сервера"));
    }

    @Test
    public void execute_trackLinkCommand() {
        trackLinkCommand.execute(chatId, "Name", new String[]{"https://stackoverflow.com/questions/12345"});
        verify(kafkaProducer, Mockito.times(1)).sendSubscribeLinkRequest(eq(chatId), anyString());
    }

    @Test
    public void execute_trackLinkCommand_shouldSendMessageIfLinkNotValid() {
        when(messageWrapper.sendMessage(eq(chatId), anyString())).thenReturn(Mono.empty());
        trackLinkCommand.execute(chatId, "Name", new String[]{"https://stackoverflow.com/"});
        verify(messageWrapper).sendMessage(chatId, "❌ Неверный формат ссылки. Пример: /track https://stackoverflow.com/questions/12345");
        verify(kafkaProducer, never()).sendSubscribeLinkRequest(eq(chatId), anyString());
    }

    @Test
    public void execute_getAllLinksCommand() {
        getAllLinks.execute(chatId, "Name", new String[]{});
        verify(kafkaProducer, times(1)).sendAllSubscribeLinksRequest(eq(chatId));
    }





}
