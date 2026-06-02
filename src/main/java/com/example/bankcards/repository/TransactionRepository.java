package com.example.bankcards.repository;

import com.example.bankcards.dto.DailySpendingDto;
import com.example.bankcards.dto.DailySpendingProjection;
import com.example.bankcards.dto.TransactionViewDto;
import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query(value = """
    SELECT
        CAST(transaction_date AS DATE) AS date,
        SUM(amount) AS amount
    FROM transactions
    WHERE card_id = :cardId
      AND CAST(transaction_date AS DATE) BETWEEN :startDate AND :endDate
    GROUP BY CAST(transaction_date AS DATE)
    ORDER BY date
    """, nativeQuery = true)
    List<DailySpendingProjection> findDailySpendingByCard(
            @Param("cardId") Long cardId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("""
    SELECT new com.example.bankcards.dto.TransactionViewDto(
        t.transactionId, t.amount, t.transactionDate, t.description,
        c.cardId, c.cardNumber, t.secondaryCard, u.email
    )
    FROM Transaction t
    JOIN t.mainCard c
    LEFT JOIN c.user u
    ORDER BY t.transactionDate DESC
    """)
    Page<TransactionViewDto> findAllViewDto(Pageable pageable);

    @Query("""
    SELECT new com.example.bankcards.dto.TransactionViewDto(
        t.transactionId, t.amount, t.transactionDate, t.description,
        c.cardId, c.cardNumber, t.secondaryCard, u.email
    )
    FROM Transaction t
    JOIN t.mainCard c
    LEFT JOIN c.user u
    WHERE c.cardId = :cardId
    ORDER BY t.transactionDate DESC
    """)
    Page<TransactionViewDto> findByCardId(@Param("cardId") Long cardId, Pageable pageable);

    @Query("""
    SELECT new com.example.bankcards.dto.TransactionViewDto(
        t.transactionId, t.amount, t.transactionDate, t.description,
        c.cardId, c.cardNumber, t.secondaryCard, u.email
    )
    FROM Transaction t
    JOIN t.mainCard c
    LEFT JOIN c.user u
    WHERE u.userId = :userId
    ORDER BY t.transactionDate DESC
    """)
    Page<TransactionViewDto> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}
