package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MakeTransactionDto {
    private Long cardId;

    private String description;

    private BigDecimal amount;

    private String cardToTransact;
    @JsonIgnore
    private Card cardReference;
}
