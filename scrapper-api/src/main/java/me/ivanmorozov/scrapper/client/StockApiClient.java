package me.ivanmorozov.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ivanmorozov.scrapper.metrics.ScrapperMetrics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

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
                .onStatus(HttpStatusCode::isError,
                        err -> Mono.error(new WebClientResponseException(err.statusCode().value(),"Ошибка при подключении к API", HttpHeaders.EMPTY,null,null)))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .flatMap(response -> parsingPrice(response, ticker))
                .doOnSuccess(__->scrapperMetrics.recordApiCallSuccess("moex-getPrice-API"))
                .onErrorResume(e ->
                    Mono.error(new RuntimeException("Ошибка при получении цены акции  " + ticker + "\n" + e.getMessage()))
                );
    }

    public Mono<String> getTicker(String ticker){
        String URI = ticker + "/marketdata.json?iss.meta=off";
        return webClient.get()
                .uri(URI)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        err -> Mono.error(new WebClientResponseException(err.statusCode().value(),"Ошибка при подключении к API", HttpHeaders.EMPTY,null,null)))
                .bodyToMono(JsonNode.class)
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

    private Mono<String> parsingNameTicket(JsonNode jsonResponse, String ticker) {
        try {
            String nameTicker = jsonResponse.path("securities")
                    .path("data")
                    .get(0)
                    .get(0)
                    .asText();
            if (nameTicker.isBlank()){
                return Mono.error(new RuntimeException("В ответе пришло пустое значение"));
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
