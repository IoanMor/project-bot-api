package me.ivanmorozov.telegrambot.serviceTest;

import me.ivanmorozov.common.kafka.MessageTypes;
import me.ivanmorozov.common.records.KafkaRecords;
import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import me.ivanmorozov.telegrambot.service.RegistrationService;
import me.ivanmorozov.telegrambot.service.TelegramSendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @Mock
    private RegistrationCache cache;
    @Mock
    private TelegramKafkaProducer kafkaProducer;
    @Mock
    private TelegramSendMessage telegramSendMessage;
    @Mock
    private TelegramLongPollingBot bot;
    @InjectMocks
    RegistrationService registrationService;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void isChatRegister_shouldTrueIfUserRegisterAndCacheInfo() throws TelegramApiException {
        Mockito.when(cache.isRegistered(1L)).thenReturn(true);
        assertTrue(registrationService.isChatRegister(1L, bot));
        verify(kafkaProducer, never()).sendRequest(anyLong(), any());
    }

    @Test
    public void isChatRegister_shouldSendKafkaAndReturnBooleanIfUserNotCached() throws TelegramApiException {
        Mockito.when(cache.isRegistered(1L)).thenReturn(null).thenReturn(false);
        assertFalse(registrationService.isChatRegister(1L, bot));
        verify(kafkaProducer, times(1)).sendIsChatRegisterRequest(1L);
        verify(telegramSendMessage, never()).sendMessage(any(), anyLong(), anyString());
    }
}
