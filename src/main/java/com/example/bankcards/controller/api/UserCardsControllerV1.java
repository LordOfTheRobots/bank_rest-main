package com.example.bankcards.controller.api;

import com.example.bankcards.dto.*;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CloudinaryRestService;
import com.example.bankcards.service.TransactionsService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
public class UserCardsControllerV1 {

    private static final Logger USER_LOG = LoggerFactory.getLogger("USER_LOG");

    private final CardService cardService;
    private final CloudinaryRestService cloudinaryService;
    private final TransactionsService transactionsService;
    private final UserService userService;

    @PostMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addCard(@AuthenticationPrincipal UserPrincipal user,
                                        @Valid @RequestBody CardEnteredDto dto) {
        try {
            USER_LOG.info("User {} adding card", user.getUser().getUserId());
            cardService.addCard(dto, user.getUser().getUserId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            USER_LOG.error("Failed to add card for user {}: {}", user.getUser().getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/cards/{cardId}")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<Void> deleteCard(@AuthenticationPrincipal UserPrincipal user,
                                           @PathVariable Long cardId) {
        try {
            USER_LOG.info("User {} deleting card {}", user.getUser().getUserId(), cardId);
            cardService.deleteCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            USER_LOG.error("Failed to delete card {} for user {}: {}", cardId, user.getUser().getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardToShowDto>> showCards(@AuthenticationPrincipal UserPrincipal user,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        USER_LOG.debug("User {} viewing cards, page: {}, size: {}", user.getUser().getUserId(), page, size);
        var cards = cardService.showCards(user.getUser().getUserId(), page, size);
        cards.forEach(card -> USER_LOG.debug(card.getCardCondition().getConditionName().getCode().toString()));
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/cards/{cardId}")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<CardToShowDto> getCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @PatchMapping("/cards/{cardId}/block")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<Void> blockCard(@AuthenticationPrincipal UserPrincipal user,
                                          @PathVariable Long cardId) {
        try {
            USER_LOG.info("User {} blocking card {}", user.getUser().getUserId(), cardId);
            cardService.blockCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            USER_LOG.error("Failed to block card {} for user {}: {}", cardId, user.getUser().getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/cards/{cardId}/image")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<Void> uploadCardImage(@AuthenticationPrincipal UserPrincipal user,
                                                @PathVariable Long cardId,
                                                @RequestParam("file") @NotNull MultipartFile file) throws Exception {
        try {
            cloudinaryService.uploadImage(cardId, file);
            USER_LOG.info("Image uploaded for card {} by user {}", cardId, user.getUser().getUserId());

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            USER_LOG.error("Failed to upload image for card {}: {}", cardId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/cards/{cardId}/transactions")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<Page<TransactionViewDto>> getCardTransactions(
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        USER_LOG.debug("User viewing transactions for card {}, page: {}", cardId, page);
        Page<TransactionViewDto> txns = transactionsService.getCardTransactions(cardId, page, size);
        return ResponseEntity.ok(txns);
    }

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TransactionViewDto>> getAllTransactions(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        USER_LOG.info(user.getUser().getEmail());
        Page<TransactionViewDto> txns = transactionsService.getUserTransactions(user.getUser().getUserId(), page, size);
        return ResponseEntity.ok(txns);
    }

    @GetMapping("/cards/{cardId}/spending")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<CardSpendingByDays> getSpendingByDays(
            @PathVariable Long cardId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        USER_LOG.debug("User requesting spending for card {} from {} to {}",
                 cardId, startDate, endDate);

        CardSpendingByDays spending = transactionsService.getSpendingByDays(cardId, startDate, endDate);
        return ResponseEntity.ok(spending);
    }


    @GetMapping("/cards/{cardId}/image")
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    public ResponseEntity<Map<String, String>> getCardImageUrl(@PathVariable Long cardId) {
        String imageUrl = cloudinaryService.getImageUrl(cardId);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> showUser(@AuthenticationPrincipal UserPrincipal auth){
        USER_LOG.info("Sending User {}", auth.getUser().getUserId());
        return ResponseEntity.ok(userService.getUser(auth.getUser().getUserId()));
    }
    @PreAuthorize("@cardService.isCardOwner(#cardId, authentication.name)")
    @GetMapping("/cards/{cardId}/provision")
    public ResponseEntity<String> getBankToken(@PathVariable Long cardId){
        return ResponseEntity.ok(cardService.getBankToken(cardId));
    }
}
