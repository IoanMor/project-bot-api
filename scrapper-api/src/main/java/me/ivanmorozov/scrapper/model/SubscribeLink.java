package me.ivanmorozov.scrapper.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "links")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscribeLink {
    @Id
    @Column(name = "id_link")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private TelegramChat chat;

    @Column(name = "link", nullable = false, length = 100)
    private String link;

    @Column(name = "count_answer", nullable = false)
    private int countAnswer;
}
