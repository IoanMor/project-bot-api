package me.ivanmorozov.scrapper.kafkaTest;

import me.ivanmorozov.scrapper.kafka.ScrapperKafkaConsumer;
import me.ivanmorozov.scrapper.services.CheckSubscribeService;
import org.hibernate.annotations.Check;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(ScrapperKafkaConsumer.class)
public class ScrapperKafkaConsumerTest {

    @Mock
    private ScrapperKafkaConsumer scrapperKafkaConsumer;

    @Test
    void shouldCallCheckSubscribeService_whenMessageReceived() {



    }

}
