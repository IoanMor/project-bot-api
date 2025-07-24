package me.ivanmorozov.telegrambot.commandTest;

import me.ivanmorozov.telegrambot.core.BotCommandHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CommandDispatcherTest {

    @Test
    public void dispatch_command(){
        BotCommandHandler mockHandler = mock(BotCommandHandler.class)
    }

}
