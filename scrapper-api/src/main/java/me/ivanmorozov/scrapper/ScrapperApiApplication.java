package me.ivanmorozov.scrapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ScrapperApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrapperApiApplication.class, args);
    }

}
