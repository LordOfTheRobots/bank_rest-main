package com.example.bankcards.controller.api;

import com.example.bankcards.dto.CardSpendingByDays;
import com.example.bankcards.dto.MakeTransactionDto;
import com.example.bankcards.exception.TransactedMoneyIsNegativeOrZero;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactControllerV1 {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final TransactionsService transactionsService;
    @PreAuthorize("@cardService.isCardOwner(#transactionDto.getCardId(), authentication.name)")
    @PostMapping("/make-transaction")
    public void makeTransaction(@AuthenticationPrincipal UserPrincipal auth,
                                @Valid @RequestBody MakeTransactionDto transactionDto) {
        if (transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactedMoneyIsNegativeOrZero("Amount must be positive");
        }
        try {
            APP_LOG.info("Making transaction from card {} to card {}, amount: {}",
                    transactionDto.getCardId(), transactionDto.getCardToTransact(), transactionDto.getAmount());
            transactionsService.makeTransaction(transactionDto);
            APP_LOG.info("Transaction completed successfully");
        } catch (Exception e) {
            APP_LOG.error("Transaction failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    @GetMapping("/spending/by-days")
    public ResponseEntity<CardSpendingByDays> getSpendingByDays(
            @RequestParam Long cardId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(transactionsService.getSpendingByDays(cardId, startDate, endDate));
    }
}
