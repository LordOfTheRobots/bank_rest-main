package com.example.bankcards.controller.api;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.NotificationService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminControllerV1 {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final CardService cardService;
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;

    @PostMapping("/card")
    public ResponseEntity<Void> createCard(@Valid @RequestBody CardEnteredByAdminDto dto) {
        try {
            APP_LOG.info("Admin creating card for user: {}", dto.getUserId());
            cardService.addCard(dto.getCard(), dto.getUserId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to create card for user {}: {}", dto.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/card")
    public ResponseEntity<Void> deleteCard(@Valid @RequestBody Long cardId) {
        try {
            APP_LOG.info("Admin deleting card: {}", cardId);
            cardService.deleteCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to delete card {}: {}", cardId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/block-card")
    public ResponseEntity<Void> blockCard(@Valid @RequestBody Long cardId) {
        try {
            APP_LOG.info("Admin blocking card: {}", cardId);
            cardService.blockCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to block card {}: {}", cardId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/user")
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserDto dto) {
        try {
            APP_LOG.info("Admin creating user: {}", dto.getEmail());
            userAuthService.createUser(dto);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to create user {}: {}", dto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/user")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserDto dto) {
        try {
            APP_LOG.info("Admin updating user: {}", dto.getEmail());
            userService.patchUser(dto);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to update user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteUser(@Valid @RequestBody UUID userId) {
        try {
            APP_LOG.info("Admin deleting user: {}", userId);
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            APP_LOG.error("Admin failed to delete user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<Card>> showCards(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        APP_LOG.info("Admin viewing all cards, page: {}, size: {}", page, size);
        return ResponseEntity.ok(cardService.showCards(page, size));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> showUsers(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        APP_LOG.info("Admin viewing all users, page: {}, size: {}", page, size);
        return ResponseEntity.ok(userService.showUsers(page, size));
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<CardToShowDto> getCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findUserById(userId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionViewDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long cardId) {

        var pageable = PageRequest.of(page, size);
        Page<TransactionViewDto> data = (cardId != null)
                ? transactionRepository.findByCardId(cardId, pageable)
                : transactionRepository.findAllViewDto(pageable);

        return ResponseEntity.ok(data);
    }
    @GetMapping("/types")
    public ResponseEntity<List<NotificationTypeDto>> getTypes() {
        return ResponseEntity.ok(notificationService.getAllTypes()); }
    @PostMapping("/types")
    public ResponseEntity<NotificationTypeDto> createType(@Valid @RequestBody NotificationTypeCommand cmd) {
        return ResponseEntity.ok(notificationService.createType(cmd));
    }
    @PutMapping("/types/{code}")
    public ResponseEntity<NotificationTypeDto> updateType(@PathVariable String code,
                                                          @Valid @RequestBody NotificationTypeCommand cmd) {
        return ResponseEntity.ok(notificationService.updateType(code, cmd));
    }
    @DeleteMapping("/types/{code}")
    public ResponseEntity<Void> deleteType(@PathVariable String code) {
        notificationService.deleteType(code); return ResponseEntity.noContent().build();
    }

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplateDto>> getTemplates(@RequestParam(required = false) String typeCode) {
        return ResponseEntity.ok(typeCode != null ? notificationService.getTemplatesByType(typeCode) :
                notificationService.getAllTemplates());
    }
    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplateDto> createTemplate(@Valid @RequestBody NotificationTemplateCommand cmd) {
        return ResponseEntity.ok(notificationService.createTemplate(cmd));
    }
    @PutMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(@PathVariable Long id,
                                                                  @Valid @RequestBody NotificationTemplateCommand cmd) {
        return ResponseEntity.ok(notificationService.updateTemplate(id, cmd));
    }
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/types/{code}")
    public ResponseEntity<NotificationTypeDto> patchType(
            @PathVariable String code,
            @Valid @RequestBody NotificationTypePatchDto dto) {
        return ResponseEntity.ok(notificationService.patchType(code, dto));
    }

    @PatchMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplateDto> patchTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplatePatchDto dto) {
        return ResponseEntity.ok(notificationService.patchTemplate(id, dto));
    }
}