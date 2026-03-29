package com.example.bankcards.controller.api;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtProvider;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/")
@AllArgsConstructor
@Slf4j
public class AdminControllerV1 {
    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping("/block-card")
    public ResponseEntity<Void> blockCard(@Valid @RequestBody Long cardId){
        log.info("Admin blocking card: {}", cardId);
        cardService.blockCard(cardId);
        log.info("Card blocked successfully by admin");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/card")
    public ResponseEntity<Void> deleteCard(@Valid @RequestBody Long cardId){
        log.info("Admin deleting card: {}", cardId);
        cardService.deleteCard(cardId);
        log.info("Admin deleted card");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/card")
    public ResponseEntity<Void> createCard(@Valid @RequestBody CardEnteredByAdminDto cardEnteredDto){
        log.info("Admin creating card for user: {}", cardEnteredDto.getUserId());
        cardService.addCard(cardEnteredDto.getCard(), cardEnteredDto.getUserId());
        log.info("Card created successfully by admin for user: {}", cardEnteredDto.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteUser(@Valid @RequestBody UUID userId){
        log.info("Admin deleting user: {}", userId);
        userService.deleteUser(userId);
        log.info("User deleted successfully by admin: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user")
    public ResponseEntity<Void> createUser(@Valid User user){
        log.info("Admin creating user: {}", user.getEmail());
        userAuthService.createUser(user);
        log.info("User created successfully by admin: {}", user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<Card>> showCards(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size){
        log.info("Admin viewing all cards, page: {}, size: {}", page, size);
        return ResponseEntity.ok(cardService.showCards(page, size));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> showUsers(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Admin viewing all users, page: {}, size: {}", page, size);
        return ResponseEntity.ok(userService.showUsers(page, size));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CardToShowDto> getCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findUserById(userId));
    }

    @PatchMapping("/users")
    public ResponseEntity<Void> updateUser(@RequestBody UserDto user) {
        userService.patchUser(user);
        return ResponseEntity.noContent().build();
    }
}