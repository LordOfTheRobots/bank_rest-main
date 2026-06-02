package com.example.bankcards.util.bankUtils;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardCondition;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardConditionRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyBankUtil implements BankUtil {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final Random random = new Random();
    private final CardRepository cardRepository;
    private final CardConditionRepository cardConditionRepository;

    @Override
    @Transactional
    public void makeTransaction(Card card, String cardNumberWhereTransact, BigDecimal howManyToTransact) {
        APP_LOG.info("Processing transaction: card={}, target={}, amount={}",
                card.getCardNumber(), cardNumberWhereTransact, howManyToTransact);

        if (card.getCondition() == null || card.getCondition().isEmpty() ||
                !card.getCondition().get(0).getConditionName().getIsAvailable()) {
            APP_LOG.error("Card {} is blocked or not usable for transactions", card.getCardNumber());
            throw new IllegalStateException("Card is not usable for transactions");
        }

        card.setBalance(card.getBalance().subtract(howManyToTransact));
        APP_LOG.info("Transaction completed successfully. New balance: {}", card.getBalance());
    }

    @Override
    @Transactional
    public Card checkCardCondition(Card card) {
        APP_LOG.debug("Checking condition for card: {}", card.getCardNumber());

        Optional<CardCondition> latest = cardConditionRepository
                .findLatestByCardId(card.getCardId());

        if (latest.isEmpty()) {
            CardCondition initial = CardCondition.builder()
                    .card(card)
                    .conditionName(CardStatus.ACTIVE)
                    .comment("auto_created_on_add")
                    .dateOfCondition(new Timestamp(System.currentTimeMillis()))
                    .build();
            cardRepository.save(card);
            cardConditionRepository.save(initial);
            var cardConditions = new ArrayList<CardCondition>();
            cardConditions.add(initial);
            card.setCondition(cardConditions);
        }

        return card;
    }

    @Override
    @Transactional
    public Card checkCardBalance(Card card) {
        APP_LOG.debug("Checking/fetching balance for card: {}", card.getCardNumber());
        // Временная заглушка для тестов без подключения к реальному банку
        if (card.getBalance() == null || card.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            card.setBalance(BigDecimal.valueOf(10000 + random.nextInt(90000)));
        }
        cardRepository.save(card);
        return card;
    }

    @Override
    public void makeBankToken(Card card) {
        APP_LOG.debug("Generating bank token for card: {}", card.getCardNumber());
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        card.setBankToken(token);
    }

    @Override
    public void blockCard(Card card) {
        APP_LOG.info("Blocking card: {}", card.getCardNumber());
        if (card.getCondition() != null || !card.getCondition().isEmpty()) {
            if (card.getCardId() == null) {
                card = cardRepository.save(card);
            }

            var cardCondition = CardCondition.builder()
                    .conditionName(CardStatus.BLOCKED)
                    .card(card)
                    .comment("added")
                    .build();

            cardConditionRepository.save(cardCondition);

            if (card.getCondition() == null) {
                card.setCondition(new ArrayList<>());
            }
            card.getCondition().add(cardCondition);
        }
    }
}