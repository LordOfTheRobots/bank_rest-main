package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "card_conditions")
@Data
@Builder
public class CardCondition {
    @Id
    private Integer conditionId;

    @Column(name = "name")
    private String conditionName;

    @Column(name = "usable")
    private Boolean isUsable;
}
