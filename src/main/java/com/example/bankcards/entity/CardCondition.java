package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "card_conditions")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CardCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID conditionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private CardStatus conditionName;

    @Column(name = "timestamp")
    private Timestamp dateOfCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "comment")
    private String comment;
}
