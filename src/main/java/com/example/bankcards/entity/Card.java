package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @Column(name = "expire_date", nullable = false, length = 5)
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
            message = "Expire date must be in format MM/YY")
    private String expireDate;

    @Column(name = "balance")
    private BigDecimal balance;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("dateOfCondition DESC")
    @JsonManagedReference
    private List<CardCondition> condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactions")
    @OrderBy("transactions DESC")
    private List<Transaction> transactions;

    @Column(name = "cloudinary_public_id", unique = true)
    private String cloudinaryPublicId;

    @Column(name = "bank_token")
    private String bankToken;

    @Transient
    private CardCondition resolvedCondition;
}
