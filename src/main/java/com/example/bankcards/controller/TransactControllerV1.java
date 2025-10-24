package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.service.TransactionsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@PreAuthorize("@userService.isOwner(#userId, authentication.name)")
@AllArgsConstructor
public class TransactControllerV1 {
    @Autowired
    private TransactionsService transactionsService;

    @PostMapping
    @RequestMapping("/{cardId}")
    @PreAuthorize("@cardService.isCardOwner(#cardId, #userId)")
    public void transactMoneyToAnotherCard(@Valid @RequestBody CardEnteredDto card,
                                           @PathVariable Long cardId,
                                           @Pattern(regexp = "^[0-9]{12,19}$") @RequestParam String cardNumberTransactTo,
                                           @PathVariable UUID userId){
        transactionsService.makeTransaction(card, cardNumberTransactTo, userId);
    }
}
