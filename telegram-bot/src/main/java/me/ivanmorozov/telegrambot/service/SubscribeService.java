package me.ivanmorozov.telegrambot.service;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.telegrambot.client.ScrapperApiClient;
import me.ivanmorozov.telegrambot.client.StackExchangeClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscribeService {
private final ScrapperApiClient scrapperApiClient;
private final StackExchangeClient stackClient;
    public void checkUpdates(){

    }
}
