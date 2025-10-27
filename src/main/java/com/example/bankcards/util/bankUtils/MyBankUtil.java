package com.example.bankcards.util.bankUtils;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardCondition;
import com.example.bankcards.repository.CardConditionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
public class MyBankUtil implements BankUtil {

    private static final Logger logger = LoggerFactory.getLogger(MyBankUtil.class);
    private final Random random = new Random();

    @Autowired
    private CardConditionRepository cardConditionRepository;

    @Override
    @Transactional
    public void makeTransaction(Card card, String cardNumberWhereTransact, Double howManyToTransact) {
        logger.info("Making transaction from card {} to card {}", card.getCardNumber(), cardNumberWhereTransact);

        if (!card.getCondition().getIsUsable()) {
            logger.error("Card {} is not usable for transactions", card.getCardNumber());
            throw new IllegalStateException("Card is not usable for transactions");
        }
        logger.debug("Transaction amount: {}", howManyToTransact);

        card.setBalance(card.getBalance().subtract(BigDecimal.valueOf(howManyToTransact)));
        //checkCardBalance(card); //Эта строчка в итоговом проекте должна заменить предыдущую строчку, но без доступа к банку используем верхнюю
        logger.info("Transaction completed successfully");
    }

    @Override
    public Card checkCardCondition(Card card) {
        logger.debug("Checking condition for card: {}", card.getCardNumber());
        if (card.getCondition() == null){
            logger.debug("Card condition is null, generating random condition");
            card.setCondition(cardConditionRepository.findById(1 + random.nextInt(3)).get());
            return card;
        }
        return card;
    }

    @Override
    public Card checkCardBalance(Card card) {
        logger.debug("Checking balance for card: {}", card.getCardNumber());
        card.setBalance(BigDecimal.valueOf(1 + random.nextInt(100000)));
        return card;
    }

    @Override
    public void makeBankToken(Card card) {
        logger.debug("Generating bank token for card: {}", card.getCardNumber());
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        card.setBankToken(token);
        logger.debug("Bank token generated successfully");
    }

    @Override
    public void blockCard(Card card) {
        logger.info("Blocking card: {}", card.getCardNumber());
        card.setCondition(cardConditionRepository.findById(2).get());
        logger.info("Card blocked successfully");
    }

    @Override
    public void unblockCard(Card card) {
        logger.info("Unblocking card: {}", card.getCardNumber());
        if (!card.getCondition().getConditionName().equals("Expired")){
            card.setCondition(cardConditionRepository.findById(1).get());
            logger.info("Card unblocked successfully");
        } else {
            logger.warn("Cannot unblock expired card: {}", card.getCardNumber());
        }
    }
}
