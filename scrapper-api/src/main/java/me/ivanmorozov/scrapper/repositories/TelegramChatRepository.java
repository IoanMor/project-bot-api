package me.ivanmorozov.scrapper.repositories;

import me.ivanmorozov.scrapper.model.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface TelegramChatRepository extends JpaRepository<TelegramChat,Long> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tg_chats (chat_id, registered_at) VALUES (:chatId, :createdAt) ON CONFLICT (chat_id) DO NOTHING", nativeQuery = true)
    void insertNewChat(@Param("chatId") long chatId, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = "SELECT chat_id FROM tg_chats", nativeQuery = true)
    Set<Long> getAllChats();

    @Query(value = "SELECT chat_id FROM tg_chats WHERE chat_id=:chatId", nativeQuery = true)
    boolean existChat (@Param("chatId") long chatId);

    @Query(value = "SELECT registered_at FROM tg_chats WHERE chat_id = :chatId", nativeQuery = true)
    LocalDateTime getRegisterTime(@Param("chatId") long chatId);
}
