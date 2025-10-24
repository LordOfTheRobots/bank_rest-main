package com.example.bankcards.util.bankUtils;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardCondition;
import com.example.bankcards.repository.CardConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
public class MyBankUtil implements BankUtil { //это класс-заглушка чтобы хоть что-то возвращалось

    private final Random random = new Random();

    @Autowired
    private CardConditionRepository cardConditionRepository;

    @Override
    @Transactional
    public void makeTransaction(Card card, String cardNumberWhereTransact) {
        if (!card.getCondition().getIsUsable()) {
            throw new IllegalStateException("Card is not usable for transactions");
        }

        BigDecimal amount = BigDecimal.valueOf(100 + random.nextInt(900));
        card.setBalance(card.getBalance().subtract(amount));
    }

    @Override
    public CardCondition checkCardCondition(Card card) {
        if (card.getCondition() == null){
            return cardConditionRepository.findById(1 + random.nextInt(3)).get();
        }
        return card.getCondition();
    }

    @Override
    public BigDecimal checkCardBalance(Card card) {
        return card.getBalance();
    }

    @Override
    public void makeBankToken(Card card) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        card.setBankToken(token);
    }

    @Override
    public void blockCard(Card card) {
        card.setCondition(cardConditionRepository.findById(2).get());

    }

    @Override
    public void unblockCard(Card card) {
        if (!card.getCondition().getConditionName().equals("Expired")){
            card.setCondition(cardConditionRepository.findById(1).get());
        }
    }


}
