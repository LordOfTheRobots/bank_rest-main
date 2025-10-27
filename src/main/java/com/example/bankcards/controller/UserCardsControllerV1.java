package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@AllArgsConstructor
@PreAuthorize("@userService.isOwner(#userId, authentication.name)")
public class UserCardsControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserCardsControllerV1.class);

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @PostMapping
    @RequestMapping("/add-card")
    public void addCard(@Valid @RequestBody CardEnteredDto cardEnteredDto,
                        @PathVariable UUID userId,
                        Authentication authentication){
        logger.info("Adding card for user: {}", userId);
        cardService.addCard(cardEnteredDto, userId);
        logger.info("Card added successfully for user: {}", userId);
    }

    @DeleteMapping
    @RequestMapping("/delete-card")
    public void deleteCard(@Valid @RequestBody CardEnteredDto card,
                           @PathVariable UUID userId,
                           Authentication authentication){
        logger.info("Deleting card for user: {}", userId);
        cardService.deleteCard(card);
        logger.info("Card deleted successfully for user: {}", userId);
    }

    @GetMapping
    @RequestMapping("/show-cards")
    public ResponseEntity<Page<CardToShowDto>> showCards(@PathVariable UUID userId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         Authentication authentication){
        logger.debug("Showing cards for user: {}, page: {}, size: {}", userId, page, size);
        Page<CardToShowDto> cards = cardService.showCards(userId, page, size);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @RequestMapping("/block-card")
    public void blockCard(@Valid @RequestBody CardEnteredDto card,
                          @PathVariable UUID userId,
                          Authentication authentication){
        logger.info("User blocking card: {}", card.getCardNumber());
        cardService.blockCard(card);
        logger.info("Card blocked successfully by user");
    }

    @PostMapping
    @RequestMapping("/unblock-card")
    public void unblockCard(@Valid @RequestBody CardEnteredDto card,
                            @PathVariable UUID userId,
                            Authentication authentication){
        logger.info("User unblocking card: {}", card.getCardNumber());
        cardService.unblockCard(card);
        logger.info("Card unblocked successfully by user");
    }
}
