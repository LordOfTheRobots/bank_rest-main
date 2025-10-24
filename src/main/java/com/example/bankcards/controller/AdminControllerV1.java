package com.example.bankcards.controller;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping
    @RequestMapping("/block-card")
    public void blockCard(@Valid @RequestBody CardEnteredDto card){
        cardService.blockCard(card);
    }

    @PostMapping
    @RequestMapping("/unblock-card")
    public void unblockCard(@Valid @RequestBody CardEnteredDto card){
        cardService.unblockCard(card);
    }

    @PutMapping
    @RequestMapping("/create-card")
    public void createCard(@Valid @RequestBody CardEnteredDto cardEnteredDto,
                           @RequestParam UUID userId){
        cardService.addCard(cardEnteredDto, userId);
    }


    @DeleteMapping
    @RequestMapping("/delete-user")
    public void deleteUser(UUID userId){
        userService.deleteUser(userId);
    }

    @PutMapping
    @RequestMapping("/create-user")
    public void createUser(@Valid User user){
        userAuthService.createUser(user);
    }

    @GetMapping
    @RequestMapping("/get-cards")
    public ResponseEntity<Page<Card>> showCards(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size){
        Page<Card> cards = cardService.showCards(page, size);
        return ResponseEntity.ok(cards);
    }
}
