package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_conditions")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CardCondition {
    @Id
    private Integer conditionId;

    @Column(name = "name")
    private String conditionName;

    @Column(name = "usable")
    private Boolean isUsable;
}
