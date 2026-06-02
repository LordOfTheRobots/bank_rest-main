package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.MakeTransactionDto;
import com.example.bankcards.entity.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("makeTransactionMapper")
public class MakeTransactionMapper implements DtoMapper<Transaction, MakeTransactionDto>{
    @Override
    public Transaction map(MakeTransactionDto dto) {
        return Transaction.builder()
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .mainCard(dto.getCardReference())
                .secondaryCard(dto.getCardToTransact())
                .build();
    }
}
