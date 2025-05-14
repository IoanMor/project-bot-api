package me.ivanmorozov.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramChatBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TelegramChatBotApplication.class, args);
	}

}
