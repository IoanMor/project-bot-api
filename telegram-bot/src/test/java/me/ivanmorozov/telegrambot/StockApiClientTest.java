package me.ivanmorozov.telegrambot;


import me.ivanmorozov.scrapper.client.StockApiClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;

public class StockApiClientTest {

    private MockWebServer mockWebServer;
    private StockApiClient stockApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Временная замена системной переменной
        String originalUrl = System.getProperty("stock.api.url");
        System.setProperty("stock.api.url", mockWebServer.url("/").toString());

        stockApiClient = new StockApiClient();

        // Восстановление оригинального значения (если было)
        if (originalUrl != null) {
            System.setProperty("stock.api.url", originalUrl);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void getPrice_ReturnsPrice_WhenResponseValid() throws Exception {
        String jsonResponse = """
            {
                "chart": {
                    "result": [
                        {
                            "meta": {
                                "regularMarketPrice": 175.32
                            }
                        }
                    ]
                }
            }""";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        StepVerifier.create(stockApiClient.getPrice("AAPL"))
                .expectNextMatches(price ->
                        price.compareTo(new BigDecimal("175.32")) == 0)
                .verifyComplete();
    }
}
