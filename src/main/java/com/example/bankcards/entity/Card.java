package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(name = "card_number", nullable = false, length = 19)
    @Pattern(regexp = "^[0-9]{12,19}$", message = "Card number must contain 12 to 19 digits")
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bank_token")
    private String bankToken;
}
