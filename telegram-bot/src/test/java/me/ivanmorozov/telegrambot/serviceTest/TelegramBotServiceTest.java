package me.ivanmorozov.telegrambot.serviceTest;

import jakarta.annotation.PostConstruct;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import me.ivanmorozov.telegrambot.config.TelegramBotConfig;
import me.ivanmorozov.telegrambot.core.CommandDispatcher;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import me.ivanmorozov.telegrambot.service.TelegramBotService;
import me.ivanmorozov.telegrambot.service.TelegramSendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotServiceTest {

    @Mock
    private TelegramBotConfig botConfig;

    @Mock
    private RegistrationCache cache;

    @Mock
    private TelegramKafkaProducer kafkaProducer;

    @Mock
    private CommandDispatcher commandDispatcher;

    @Mock
    private TelegramSendMessage telegramSendMessage;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private TelegramBotService telegramBotService;

    @BeforeEach
    public void setup(){
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getChat()).thenReturn(chat);

    }


    @Test
    void onUpdateReceived_shouldSendMessageIfUserNotRegistered() throws TelegramApiException {
        when(message.getText()).thenReturn("/track");
        when(message.getChatId()).thenReturn(1L);
        when(chat.getFirstName()).thenReturn("TestUser");
        when(cache.isRegistered(1L)).thenReturn(false);

        telegramBotService.onUpdateReceived(update);

        verify(telegramSendMessage).sendMessage(
                any(TelegramBotService.class),
                eq(1L),
                contains("зарегистрироваться")
        );

        verify(commandDispatcher, never()).dispatch(any(), anyLong(), any());
    }

}
