package com.example.bankcards.controller.api;

import com.example.bankcards.dto.CardEnteredByAdminDto;
import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardOperation;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.security.JwtProvider;
import com.example.bankcards.service.CardService;
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
@RequestMapping("/v1/user")
@AllArgsConstructor
public class UserCardsControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserCardsControllerV1.class);

    @Autowired
    private CardService cardService;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/add-card")
    public void addCard(Authentication authentication,
                        @Valid @RequestBody CardEnteredDto cardEnteredDto){
        logger.info("Adding card for user: {}", authentication.getName());
        cardService.addCard(cardEnteredDto, UUID.fromString(authentication.getName()));
        logger.info("Card added successfully for user: {}", authentication.getName());
    }

    @PreAuthorize("@cardService.isCardOwner(card.getCardId(), authentication.name)")
    @DeleteMapping("/delete-card")
    public void deleteCard(Authentication authentication,
                           @Valid @RequestBody Long cardId){
        logger.info("Deleting card for user: {}", authentication.getName());
        cardService.deleteCard(cardId);
        logger.info("Card deleted successfully for user: {}", authentication.getName());
    }

    @GetMapping("/show-cards")
    public ResponseEntity<Page<CardToShowDto>> showCards(Authentication authentication,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        logger.debug("Showing cards for user: {}, page: {}, size: {}", authentication.getName(), page, size);
        Page<CardToShowDto> cards = cardService.showCards(authentication.getName(), page, size);
        return ResponseEntity.ok(cards);
    }


    @PreAuthorize("@cardService.isCardOwner(card.getCardId(), authentication.name)")
    @PostMapping("/block-card")
    public void blockCard(@Valid @RequestBody Long cardId){
        logger.info("User blocking card");
        cardService.blockCard(cardId);
        logger.info("Card blocked successfully by user");
    }
}
