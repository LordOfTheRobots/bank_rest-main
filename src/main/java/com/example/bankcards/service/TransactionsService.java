package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.NotEnoughMoney;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.bankUtils.MyBankUtil;
import com.example.bankcards.util.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionsService {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final @Qualifier("makeTransactionMapper") DtoMapper<Transaction,MakeTransactionDto> transactionDtoMapper;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final MyBankUtil bankUtil;

    @Transactional
    public void makeTransaction(MakeTransactionDto transactionDto){
        APP_LOG.info("Making transaction from card: {} to card: {}",
                transactionDto.getCardId(), transactionDto.getCardToTransact());

        try {
            Card card = cardRepository.findById(transactionDto.getCardId())
                    .orElseThrow(() -> new NotFound("No such card"));

            if (bankUtil.checkCardBalance(card).getBalance().compareTo(transactionDto.getAmount()) <= 0){
                throw new NotEnoughMoney("Not enough money to transact");
            }

            bankUtil.makeTransaction(card, transactionDto.getCardToTransact(), transactionDto.getAmount());
            //bankUtil.checkCardBalance(cardToTransact.get());
            transactionDto.setCardReference(cardRepository.getReferenceById(transactionDto.getCardId()));
            transactionRepository.save(transactionDtoMapper.map(transactionDto));
            cardRepository.save(card);
            APP_LOG.info("Transaction completed successfully");
        } catch (Exception e){
            APP_LOG.error("Transaction failed: {}", e.getMessage(), e);
        }
    }

    public CardSpendingByDays getSpendingByDays(Long cardId, LocalDate startDate, LocalDate endDate) {
        List<DailySpendingProjection> raw = transactionRepository.findDailySpendingByCard(cardId, startDate, endDate);

        List<DailySpendingDto> days = raw.stream()
                .map(p -> new DailySpendingDto(p.getDate(), p.getAmount()))
                .toList();

        return new CardSpendingByDays(cardId, startDate, endDate, days);
    }

    public Page<TransactionViewDto> getUserTransactions(UUID userId, Integer page, Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByUserId(userId, pageable);
    }
    public Page<TransactionViewDto> getCardTransactions(Long cardId, Integer page, Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByCardId(cardId, pageable);
    }
}