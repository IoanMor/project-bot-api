package me.ivanmorozov.telegramchatbot;

import me.ivanmorozov.telegramchatbot.config.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class TelegramChatBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TelegramChatBotApplication.class, args);
	}

}
