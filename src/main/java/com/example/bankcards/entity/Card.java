package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    private Long cardId;

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @Column(name = "expire_date", nullable = false, length = 5)
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
            message = "Expire date must be in format MM/YY")
    private String expireDate;

    @Column(name = "balance")
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "condition")
    private CardCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
