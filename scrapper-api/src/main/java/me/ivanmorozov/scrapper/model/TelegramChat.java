package me.ivanmorozov.scrapper.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tg_chats")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelegramChat {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stockSubscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> linkSubscriptions = new ArrayList<>();
}
