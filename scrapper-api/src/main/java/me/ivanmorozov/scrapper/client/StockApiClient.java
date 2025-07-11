package me.ivanmorozov.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class StockApiClient {
    private final WebClient webClient;
    private final ScrapperMetrics scrapperMetrics;

    public StockApiClient(@Qualifier("stockApiWebClient") WebClient webClient, ScrapperMetrics scrapperMetrics) {
        this.webClient = webClient;
        this.scrapperMetrics = scrapperMetrics;
    }

    public Mono<BigDecimal> getPrice(String ticker) {
        String URI = ticker + "/marketdata.json?iss.meta=off";
        return webClient.get()
                .uri(URI)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> parsingPrice(response, ticker))
                .doOnSuccess(__->scrapperMetrics.recordApiCallSuccess("moex-getPrice-API"))
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка при получении цены акции  " + ticker + "\n" + e.getMessage())));
    }

    public Mono<String> getTicker(String ticker){
        String URI = ticker + "/marketdata.json?iss.meta=off";
        return webClient.get()
                .uri(URI)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> parsingNameTicket(response, ticker))
                .doOnSuccess(__->scrapperMetrics.recordApiCallSuccess("moex-getTicker-API"))
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка при получении тикера акции  " + ticker + "\n" + e.getMessage())));
    }

    private Mono<BigDecimal> parsingPrice(JsonNode jsonResponse, String ticker) {
        try {

            BigDecimal price = new BigDecimal(
                    jsonResponse.path("securities")
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

    private Mono<String> parsingNameTicket(String jsonResponse, String ticker) {
        try {
            JsonNode root = new ObjectMapper().readTree(jsonResponse);
            String nameTicker = root.path("securities")
                    .path("data")
                    .get(0)
                    .get(0)
                    .asText();
            if (nameTicker.isBlank()){
                return Mono.empty();
            }
            return Mono.just(nameTicker);

        }catch (NullPointerException ne){
            return Mono.empty();
        }
        catch (Exception e) {
            return Mono.error(new RuntimeException("Не удалось распарсить тикер акции " + ticker + "\n" + e.getMessage()));
        }
    }

    public Mono<Boolean> validateTickerName(String ticker) {
       return getTicker(ticker)
               .map(nameTicker -> Objects.equals(nameTicker,ticker))
               .defaultIfEmpty(false)
               .onErrorReturn(false);
    }

}
