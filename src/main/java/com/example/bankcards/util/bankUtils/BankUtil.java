package com.example.bankcards.util.bankUtils;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardCondition;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface BankUtil {
    @Transactional
    void makeTransaction(Card card, String cardNumberWhereTransact, Double howManyToTransact);
    Card checkCardCondition(Card card);
    Card checkCardBalance(Card card);
    void makeBankToken(Card card);
    void blockCard(Card card);
    void unblockCard(Card card);
}
