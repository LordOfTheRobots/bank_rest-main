package com.example.bankcards.controller.api;

import com.example.bankcards.dto.CardEnteredByAdminDto;
import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.exception.TransactedMoneyIsNegativeOrZero;
import com.example.bankcards.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/transaction/")
@AllArgsConstructor
public class TransactControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(TransactControllerV1.class);

    @Autowired
    private TransactionsService transactionsService;

    @PostMapping
    @PreAuthorize("@cardService.isCardOwner(card.getCardId(), userId)")
    @RequestMapping("/make-transaction")
    public void makeTransaction(@Valid @RequestBody CardEnteredDto card,
                                @RequestParam String whereToTransact,
                                @RequestParam UUID userId,
                                @RequestParam Float howManyToTransact){
        logger.info("Making transaction for user: {} to card: {}", userId, whereToTransact);
        if (howManyToTransact > 0){
            transactionsService.makeTransaction(card, whereToTransact, userId, howManyToTransact);
        }
        else {
            throw new TransactedMoneyIsNegativeOrZero("Entered amount of money is negative or zero");
        }
        logger.info("Transaction completed successfully for user: {}", userId);
    }
}
