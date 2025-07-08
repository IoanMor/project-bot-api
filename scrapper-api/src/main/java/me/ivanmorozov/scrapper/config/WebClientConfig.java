package me.ivanmorozov.scrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static me.ivanmorozov.common.apiUrl.APIUrl.STACK_API_URL;
import static me.ivanmorozov.common.apiUrl.APIUrl.STOCK_API_URL;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient stackOverFlowWebClient(){
        return WebClient.builder()
                .baseUrl(STACK_API_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient stockApiClient(){
        return WebClient.builder()
                .baseUrl(STOCK_API_URL)
                .build();
    }
}
