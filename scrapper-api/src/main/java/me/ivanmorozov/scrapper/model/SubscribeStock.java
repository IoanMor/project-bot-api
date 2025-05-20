package me.ivanmorozov.scrapper.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "stock")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscribeStock {
    @Id
    @Column(name = "id_stock")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    private TelegramChat chat;

    @Column(name = "ticker", nullable = false, length = 10)
    private String ticker;

    @Column(name = "count_stock", nullable = false)
    private int countStock;

}
