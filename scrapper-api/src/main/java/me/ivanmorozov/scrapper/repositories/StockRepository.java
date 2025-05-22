package me.ivanmorozov.scrapper.repositories;

import me.ivanmorozov.scrapper.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Repository
@Transactional
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO stock (chat_id, ticker) VALUES (:chatId, :ticker) ", nativeQuery = true)
     void insertStock(@Param("chatId") long chatId, @Param("ticker") String ticker);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stock WHERE chat_id = :chatId AND ticker = :ticker", nativeQuery = true)
    void removeStock(@Param("chatId") long chatId, @Param("ticker") String ticker);

    @Transactional
    @Query(value = "SELECT ticker FROM stock WHERE chat_id = :chatId", nativeQuery = true)
    Set<String> getTickersByChatId(@Param("chatId") long chatId);


    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Stock s WHERE s.chat.chatId = :chatId AND s.ticker = :ticker")
    boolean existsByTicker(@Param("chatId") long chatId, @Param("ticker") String ticker);
}
