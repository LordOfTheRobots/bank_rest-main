package com.example.bankcards.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "card_conditions")
public class CardCondition {
    @Id
    private Integer conditionId;

    @Column(name = "name")
    private String conditionName;

    @Column(name = "usable")
    private Boolean isUsable;
}
