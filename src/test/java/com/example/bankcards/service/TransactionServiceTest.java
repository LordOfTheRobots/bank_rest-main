package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotEnoughMoney;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.bankUtils.MyBankUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private MyBankUtil bankUtil;

    @InjectMocks
    private TransactionsService transactionsService;

    @Test
    void makeTransaction_Success() {
        CardEnteredDto cardDto = new CardEnteredDto();
        cardDto.setCardNumber("1234567812345678");
        cardDto.setExpirationDate("12/25");

        String whereToTransact = "8765432187654321";
        UUID userId = UUID.randomUUID();
        Double amount = 100.0;

        Card sourceCard = new Card();
        sourceCard.setBalance(BigDecimal.valueOf(500.0));

        Card targetCard = new Card();
        targetCard.setBalance(BigDecimal.valueOf(200.0));

        Card checkedCard = new Card();
        checkedCard.setBalance(BigDecimal.valueOf(500.0));

        when(cardRepository.findByCardNumberAndUserId(whereToTransact, userId))
                .thenReturn(Optional.of(targetCard));
        when(cardRepository.findByCardNumberAndExpireDate(anyString(), any()))
                .thenReturn(Optional.of(sourceCard));
        when(bankUtil.checkCardBalance(sourceCard)).thenReturn(checkedCard); // ← Возвращает Card

        transactionsService.makeTransaction(cardDto, whereToTransact, userId, amount);

        verify(bankUtil).makeTransaction(sourceCard, whereToTransact, amount);
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void makeTransaction_NotEnoughMoney_ThrowsException() {
        CardEnteredDto cardDto = new CardEnteredDto();
        cardDto.setCardNumber("1234567812345678");

        String whereToTransact = "8765432187654321";
        UUID userId = UUID.randomUUID();
        Double amount = 1000.0;

        Card sourceCard = new Card();
        sourceCard.setBalance(BigDecimal.valueOf(500.0));

        Card targetCard = new Card();

        Card checkedCard = new Card();
        checkedCard.setBalance(BigDecimal.valueOf(500.0));

        when(cardRepository.findByCardNumberAndUserId(whereToTransact, userId))
                .thenReturn(Optional.of(targetCard));
        when(cardRepository.findByCardNumberAndExpireDate(anyString(), any()))
                .thenReturn(Optional.of(sourceCard));
        when(bankUtil.checkCardBalance(sourceCard)).thenReturn(checkedCard); // ← Возвращает Card

        assertThrows(NotEnoughMoney.class,
                () -> transactionsService.makeTransaction(cardDto, whereToTransact, userId, amount));

        verify(bankUtil, never()).makeTransaction(any(), anyString(), anyDouble());
    }

    @Test
    void makeTransaction_TargetCardNotFound_ThrowsException() {
        CardEnteredDto cardDto = new CardEnteredDto();
        String whereToTransact = "8765432187654321";
        UUID userId = UUID.randomUUID();
        Double amount = 100.0;

        when(cardRepository.findByCardNumberAndUserId(whereToTransact, userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFound.class,
                () -> transactionsService.makeTransaction(cardDto, whereToTransact, userId, amount));

        verify(bankUtil, never()).makeTransaction(any(), anyString(), anyDouble());
    }

    @Test
    void makeTransaction_SourceCardNotFound_ThrowsException() {
        CardEnteredDto cardDto = new CardEnteredDto();
        String whereToTransact = "8765432187654321";
        UUID userId = UUID.randomUUID();
        Double amount = 100.0;

        assertThrows(NotFound.class,
                () -> transactionsService.makeTransaction(cardDto, whereToTransact, userId, amount));

        verify(bankUtil, never()).makeTransaction(any(), anyString(), anyDouble());
    }
}