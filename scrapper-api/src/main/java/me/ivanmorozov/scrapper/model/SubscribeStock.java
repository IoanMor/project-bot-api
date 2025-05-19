package me.ivanmorozov.scrapper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "stock")
@Setter
@Getter
public class SubscribeStock {
    @Id
    @Column(name = "id_stock")
    private long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private TelegramChat chatId;

    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;

    private int countStock;

}
