package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.BankUtil;
import com.example.bankcards.util.mapper.CardEnterMapper;
import com.example.bankcards.util.mapper.CardToCardShowMapper;
import com.example.bankcards.util.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest { //Без спая на некоторых моках тесты не запускаются и из-за этого они зависят друг от друга к сожалению по итогу тесты нужно запускать отдельно
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Spy
    private DtoMapper<Card, CardEnteredDto> mapper = new CardEnterMapper();

    @Mock
    private BankUtil bankUtil;

    @Mock
    private UserService userService;

    @Spy
    private DtoMapper<CardToShowDto, Card> toShowMapper = new CardToCardShowMapper();

    @InjectMocks
    private CardService cardService;

    @Test
    void deleteCard_Success() {
        CardEnteredDto cardDto = new CardEnteredDto();
        cardDto.setCardNumber("1234567890123456");
        cardDto.setExpirationDate("14/25");

        String maskedCardNumber = MaskCard.makeMaskOfCardNumber(cardDto.getCardNumber());

        cardService.deleteCard(cardDto);

        verify(cardRepository).deleteByCardNumberAndExpireDate(
                maskedCardNumber, cardDto.getExpirationDate());
    }

    @Test
    void isCardOwner_WhenCardExistsAndUserIsOwner_ReturnsTrue() {
        Long cardId = 1L;
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setUserId(userId);

        Card card = new Card();
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        boolean result = cardService.isCardOwner(cardId, userId);

        assertTrue(result);
        verify(cardRepository).findById(cardId);
    }

    @Test
    void isCardOwner_WhenCardNotFound_ThrowsNotFound() {
        Long cardId = 1L;
        UUID userId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> cardService.isCardOwner(cardId, userId));
        verify(cardRepository).findById(cardId);
    }

    @Test
    void addCard_WhenUserExists_Success() {
        UUID userId = UUID.randomUUID();
        CardEnteredDto cardEntered = new CardEnteredDto();
        cardEntered.setCardNumber("121412412414121");
        cardEntered.setExpirationDate("12/30");

        Card card = new Card();
        card.setCardNumber("121412412414121");
        card.setExpireDate("12/30");

        User user = new User();
        user.setUserId(userId);

        when(mapper.map(cardEntered)).thenReturn(card);
        when(userService.userExist(userId)).thenReturn(true);
        when(userService.findUserById(userId)).thenReturn(user);

        cardService.addCard(cardEntered, userId);

        verify(mapper).map(cardEntered);
        verify(bankUtil).checkCardCondition(card);
        verify(userService).userExist(userId);
        verify(userService).findUserById(userId);
        verify(bankUtil).checkCardBalance(card);
        verify(bankUtil).makeBankToken(card);
        verify(cardRepository).save(card);
    }

    @Test
    void addCard_WhenUserNotExists_ThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        CardEnteredDto cardEntered = new CardEnteredDto();
        cardEntered.setUserId(userId);
        cardEntered.setCardNumber("121412412414121");
        cardEntered.setExpirationDate("12.30");
        Card card = new Card();
        User user = new User();
        user.setUserId(userId);
        card.setUser(user);
        when(userService.userExist(userId)).thenReturn(false);

        assertThrows(NotFound.class, () -> cardService.addCard(cardEntered, userId));
        verify(userService).userExist(userId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void showCards_ForUser_ReturnsPage() {
        UUID userId = UUID.randomUUID();
        Integer pageNumber = 0;
        Integer pageSize = 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Card card = new Card();
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));

        when(cardRepository.findByUserUserId(userId, pageable)).thenReturn(cardPage);

        Page<CardToShowDto> result = cardService.showCards(userId, pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findByUserUserId(userId, pageable);
    }

    @Test
    void showCards_AllCards_ReturnsPage() {
        Integer pageNumber = 0;
        Integer pageSize = 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Card card = new Card();
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        Page<Card> result = cardService.showCards(pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void blockCard_WhenCardExists_Success() {
        CardEnteredDto cardEnteredDto = new CardEnteredDto();
        cardEnteredDto.setCardNumber("1234567890123456");
        cardEnteredDto.setExpirationDate("14/25");

        Card card = new Card();
        String maskedCardNumber = MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber());

        when(cardRepository.findByCardNumberAndExpireDate(
                maskedCardNumber, cardEnteredDto.getExpirationDate()))
                .thenReturn(Optional.of(card));

        cardService.blockCard(cardEnteredDto);

        verify(bankUtil).blockCard(card);
    }

    @Test
    void blockCard_WhenCardNotFound_ThrowsNotFound() {
        CardEnteredDto cardEnteredDto = new CardEnteredDto();
        cardEnteredDto.setCardNumber("1234567890123456");
        cardEnteredDto.setExpirationDate("14/25");

        String maskedCardNumber = MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber());

        when(cardRepository.findByCardNumberAndExpireDate(
                maskedCardNumber, cardEnteredDto.getExpirationDate()))
                .thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> cardService.blockCard(cardEnteredDto));
        verify(bankUtil, never()).blockCard(any());
    }

    @Test
    void unblockCard_WhenCardExists_Success() {
        CardEnteredDto cardEnteredDto = new CardEnteredDto();
        cardEnteredDto.setCardNumber("1234567890123456");
        cardEnteredDto.setExpirationDate("14/25");

        Card card = new Card();
        String maskedCardNumber = MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber());

        when(cardRepository.findByCardNumberAndExpireDate(
                maskedCardNumber, cardEnteredDto.getExpirationDate()))
                .thenReturn(Optional.of(card));

        cardService.unblockCard(cardEnteredDto);

        verify(bankUtil).unblockCard(card);
    }

    @Test
    void unblockCard_WhenCardNotFound_ThrowsNotFound() {
        CardEnteredDto cardEnteredDto = new CardEnteredDto();
        cardEnteredDto.setCardNumber("1234567890123456");
        cardEnteredDto.setExpirationDate("14/25");

        String maskedCardNumber = MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber());

        when(cardRepository.findByCardNumberAndExpireDate(
                maskedCardNumber, cardEnteredDto.getExpirationDate()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFound.class, () -> cardService.unblockCard(cardEnteredDto));
        verify(bankUtil, never()).unblockCard(any());
    }
}