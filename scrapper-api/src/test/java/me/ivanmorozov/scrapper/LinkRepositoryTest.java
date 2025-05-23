package me.ivanmorozov.scrapper;

import me.ivanmorozov.scrapper.repositories.LinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LinkRepositoryTest {
        @Autowired
        private LinkRepository linkRepository;

        @Test
        @Transactional

        public void testUpdateCountAnswer() {
            long chatId = 882850654;
            String link = "https://stackoverflow.com/questions/79633681/long-array-to-search-cause-sss-request-time-exceeded-on-suitelet-2-x";
            int newCount = 1;


            linkRepository.updateCountAnswer(chatId, link, newCount);

            int count = linkRepository.getCountAnswer(chatId, link);
            System.out.println(count);
            Assertions.assertEquals(newCount, count);
        }
    }

