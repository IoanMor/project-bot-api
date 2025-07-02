package me.ivanmorozov.scrapper.repositories;

import me.ivanmorozov.scrapper.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO links(chat_id,link) VALUES (:chatId,:link)", nativeQuery = true)
    void subscribeLink(@Param("chatId") long chatId, @Param("link") String link);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM links WHERE chat_id = :chatId AND link = :link", nativeQuery = true)
    void removeLink(@Param("chatId") long chatId, @Param("link") String link);

    @Transactional
    @Query(value = "SELECT link FROM links WHERE chat_id = :chatId", nativeQuery = true)
    Set<String> getLinks(@Param("chatId") long chatId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Link l WHERE l.chat.chatId = :chatId AND l.link = :link")
    boolean existsLink(@Param("chatId") long chatId, @Param("link") String link);

    @Query(value = "SELECT count_answer FROM links WHERE chat_id =:chatId AND link=:link", nativeQuery = true)
    int getCountAnswer(@Param("chatId") long chatId, @Param("link") String link);

    @Modifying
    @Transactional
    @Query(value = "UPDATE links SET count_answer = :count WHERE chat_id = :chatId AND link = :link", nativeQuery = true)
    void updateCountAnswer(@Param("chatId") long chatId, @Param("link") String link, @Param("count") int count);
}
