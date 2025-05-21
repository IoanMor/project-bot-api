package me.ivanmorozov.telegrambot;

import me.ivanmorozov.scrapper.client.StockApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TelegramChatBotApplicationTests {
    @ExtendWith(MockitoExtension.class)
    class StockTest {

        @InjectMocks
        private  StockApiClient stockApiClient;

        @Test
        void getStockPrice() {

        }
    }
}
