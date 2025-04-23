package me.ivan.morozov.scrapperapi.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
public class TgChat {
    @Id
    private Long chatId;
    private LocalDateTime createdAt;
    private String status = "ACTIVE";
}
