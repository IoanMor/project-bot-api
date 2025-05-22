package me.ivanmorozov.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static me.ivanmorozov.common.apiUrl.APIUrl.STOCK_API_URL;

@Component
public class StockApiClient {
    private final WebClient webClient;

    public StockApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl(STOCK_API_URL)
                .build();
    }

    public Mono<BigDecimal> getPrice(String ticker) {
        String URI = ticker+"/marketdata.json?iss.meta=off";
        return webClient.get()
                .uri(URI)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> parsingPrice(response, ticker))
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка при получении цены акции  " + ticker + "\n" + e.getMessage())));
    }
    private Mono<BigDecimal> parsingPrice(String jsonResponse, String ticker) {
        try {
            JsonNode root = new ObjectMapper().readTree(jsonResponse);
            BigDecimal price = new BigDecimal(
                    root.path("securities")
                            .path("data")
                            .get(0)
                            .get(3)
                            .asText()
            );
            return Mono.just(price);

        } catch (Exception e) {
            return Mono.error(new RuntimeException("Не удалось распарсить цену акции " + ticker + "\n" + e.getMessage()));
        }
    }

}
