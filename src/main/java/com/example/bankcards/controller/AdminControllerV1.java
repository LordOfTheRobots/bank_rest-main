package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/")
@PreAuthorize("@userService.isAdmin(authentication.name)")
@AllArgsConstructor
public class AdminControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(AdminControllerV1.class);

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping
    @RequestMapping("/block-card")
    public void blockCard(@Valid @RequestBody CardEnteredDto card){
        logger.info("Admin blocking card: {}", card.getCardNumber());
        cardService.blockCard(card);
        logger.info("Card blocked successfully by admin");
    }

    @PostMapping
    @RequestMapping("/unblock-card")
    public void unblockCard(@Valid @RequestBody CardEnteredDto card){
        logger.info("Admin unblocking card: {}", card.getCardNumber());
        cardService.unblockCard(card);
        logger.info("Card unblocked successfully by admin");
    }

    @PutMapping
    @RequestMapping("/create-card")
    public void createCard(@Valid @RequestBody CardEnteredDto cardEnteredDto,
                           @RequestParam UUID userId){
        logger.info("Admin creating card for user: {}", userId);
        cardService.addCard(cardEnteredDto, userId);
        logger.info("Card created successfully by admin for user: {}", userId);
    }

    @DeleteMapping
    @RequestMapping("/delete-user")
    public void deleteUser(UUID userId){
        logger.info("Admin deleting user: {}", userId);
        userService.deleteUser(userId);
        logger.info("User deleted successfully by admin: {}", userId);
    }

    @PutMapping
    @RequestMapping("/create-user")
    public void createUser(@Valid User user){
        logger.info("Admin creating user: {}", user.getEmail());
        userAuthService.createUser(user);
        logger.info("User created successfully by admin: {}", user.getEmail());
    }

    @GetMapping
    @RequestMapping("/get-cards")
    public ResponseEntity<Page<Card>> showCards(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size){
        logger.info("Admin viewing all cards, page: {}, size: {}", page, size);
        Page<Card> cards = cardService.showCards(page, size);
        return ResponseEntity.ok(cards);
    }
}