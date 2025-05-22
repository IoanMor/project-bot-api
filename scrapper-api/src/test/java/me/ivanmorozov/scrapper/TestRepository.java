package me.ivanmorozov.scrapper;

import me.ivanmorozov.scrapper.repositories.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class TestRepository {
    @Autowired
    private StockRepository stockRepository;

    @Test
    void insertDuplicate(){
        stockRepository.deleteAll();

        stockRepository.insertStock(1L,"AAPL");

        assertDoesNotThrow(() -> stockRepository.insertStock(1L, "AAPL"));
        assertEquals(1, stockRepository.count()); // Проверяем, что дубликат не добавился
    }
}
