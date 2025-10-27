package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.exception.TransactedMoneyIsNegativeOrZero;
import com.example.bankcards.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction/")
@PreAuthorize("@userService.isOwner(#userId, authentication.name)")
@AllArgsConstructor
public class TransactControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(TransactControllerV1.class);

    @Autowired
    private TransactionsService transactionsService;

    @PostMapping
    @RequestMapping("/make-transaction")
    public void makeTransaction(@Valid @RequestBody CardEnteredDto card,
                                @RequestParam String whereToTransact,
                                @RequestParam UUID userId,
                                @RequestParam Double howManyToTransact){
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
