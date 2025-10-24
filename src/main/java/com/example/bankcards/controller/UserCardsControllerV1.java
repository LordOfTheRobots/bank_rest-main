package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@PreAuthorize("@userService.isOwnerOrAdmin(#userId, authentication.name)")
@AllArgsConstructor
public class UserCardsControllerV1 {

    @Autowired
    private CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardToShowDto>> showCards(@PathVariable UUID userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size){
        Page<CardToShowDto> cards = cardService.showCards(userId, page, size);
        return ResponseEntity.ok(cards);
    }

    @PutMapping
    public void addCard(@PathVariable UUID userId, @Valid @RequestBody CardEnteredDto card){
        cardService.addCard(card, userId);
    }

    @DeleteMapping
    @PreAuthorize("@cardService.isCardOwner(#cardId, #userId) || @userService.isAdmin(authentication.name)")
    public void deleteCard(@Valid @RequestBody CardEnteredDto card, @RequestParam Long cardId){
        cardService.deleteCard(card);
    }

    @PostMapping
    @PreAuthorize("@cardService.isCardOwner(#cardId, #userId) || @userService.isAdmin(authentication.name)")
    public void blockCard(@Valid @RequestBody CardEnteredDto card){
        cardService.blockCard(card);
    }
}
