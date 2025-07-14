package me.ivanmorozov.scrapper.serviceTest;

import me.ivanmorozov.scrapper.client.StackOverflowClient;
import me.ivanmorozov.scrapper.kafka.ScrapperKafkaProducer;
import me.ivanmorozov.scrapper.repositories.LinkRepository;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import me.ivanmorozov.scrapper.services.ReactiveMethodsDB;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckSubscribeServiceTest {
    @Mock
    private  LinkRepository linkRepository;
    @Mock
    private  StackOverflowClient client;
    @Mock
    private  ScrapperKafkaProducer kafkaProducer;
    @Mock
    private  ReactiveMethodsDB reactiveMethod;

    @InjectMocks
    private CheckSubscribeService checkSubscribeService;
}
