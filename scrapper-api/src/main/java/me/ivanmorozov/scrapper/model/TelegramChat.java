package me.ivanmorozov.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tg_chats")
@Getter
@Setter
public class TelegramChat {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
