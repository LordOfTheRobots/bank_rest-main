package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.TransactionViewDto;
import com.example.bankcards.entity.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("transactionToViewDto")
public class TransactionViewDtoMapper implements DtoMapper<TransactionViewDto, Transaction> {
    @Override
    public TransactionViewDto map(Transaction t) {
        return new TransactionViewDto(
                t.getTransactionId(),
                t.getAmount(),
                t.getTransactionDate() != null ? t.getTransactionDate() : null,
                t.getDescription(),
                t.getMainCard() != null ? t.getMainCard().getCardId() : null,
                t.getSecondaryCard(),
                t.getSecondaryCard(),
                t.getMainCard() != null && t.getMainCard().getUser() != null
                        ? t.getMainCard().getUser().getEmail() : null
        );
    }
}
