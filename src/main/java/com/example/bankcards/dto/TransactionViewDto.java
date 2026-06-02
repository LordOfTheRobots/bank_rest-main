package com.example.bankcards.dto;

import com.example.bankcards.entity.Transaction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionViewDto(
        UUID transactionId,
        BigDecimal amount,
        Timestamp transactionDate,
        String description,
        Long mainCardId,
        String mainCardNumber,
        String secondaryCard,
        String userEmail
){
    public static TransactionViewDto fromEntity(Transaction t) {
    return new TransactionViewDto(
            t.getTransactionId(),
            t.getAmount(),
            t.getTransactionDate() != null ? t.getTransactionDate() : null,
            t.getDescription(),
            t.getMainCard() != null ? t.getMainCard().getCardId() : null,
            t.getMainCard().getCardNumber(),
            t.getSecondaryCard(),
            t.getMainCard().getUser() != null
                    ? t.getMainCard().getUser().getEmail() : null
    );
}}
