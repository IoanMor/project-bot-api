package me.ivanmorozov.scrapper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "links")
@Setter
@Getter
public class SubscribeLink {
    @Id
    @Column(name = "id_link")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private TelegramChat chat;

    @Column(name = "link", nullable = false, length = 100)
    private String link;

}
