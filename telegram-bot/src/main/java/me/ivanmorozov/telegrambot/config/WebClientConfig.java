package me.ivanmorozov.telegrambot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static me.ivanmorozov.common.apiUrl.APIUrl.TELEGRAM_API_URL;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient telegramAPIClient(){
        return WebClient
                .builder()
                .baseUrl(TELEGRAM_API_URL)
                .build();
    }
}
