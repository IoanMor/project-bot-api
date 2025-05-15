package me.ivanmorozov.scrapper.repositories;

import me.ivanmorozov.scrapper.model.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TelegramChatRepository extends JDBCRepo<TelegramChat,Long> {


}
