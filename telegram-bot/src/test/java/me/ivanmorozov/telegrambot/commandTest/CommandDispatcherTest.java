package me.ivanmorozov.telegrambot.commandTest;

import me.ivanmorozov.telegrambot.client.MessageWrapper;
import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import me.ivanmorozov.telegrambot.core.CommandDispatcher;
import me.ivanmorozov.telegrambot.kafka.TelegramKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandDispatcherTest {

    @Mock
    private BotCommandHandler commandHandler;
    @Mock
    private MessageWrapper messageWrapper;

    @InjectMocks
    private CommandDispatcher dispatcher;

    @BeforeEach
    public void init() {
        dispatcher = new CommandDispatcher(List.of(commandHandler), messageWrapper);
    }

    @Test
    public void dispatch_startCommand() {

        when(commandHandler.getCommand()).thenReturn("/start");


        dispatcher.dispatch("/start", 1L, "Name");

        verify(commandHandler).execute(1L, "Name", new String[]{});
    }

    @Test
    public void dispatch_trackCommand() {

        when(commandHandler.getCommand()).thenReturn("/track");


        dispatcher.dispatch("/track https://stackoverflow.com/questions/12345", 1L, "Name");

        verify(commandHandler).execute(1L, "Name", new String[]{"https://stackoverflow.com/questions/12345"});
    }

    @Test
    public void dispatch_linksCommand() {

        when(commandHandler.getCommand()).thenReturn("/links");


        dispatcher.dispatch("/links", 1L, "Name");

        verify(commandHandler, times(1)).execute(eq(1L), eq("Name"), eq(new String[]{}));
    }




}
