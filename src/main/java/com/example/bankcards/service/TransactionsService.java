package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotEnoughMoney;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.MyBankUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionsService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionsService.class);

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private MyBankUtil bankUtil;

    @Transactional
    public void makeTransaction(CardEnteredDto cardDto, String whereToTransact, UUID userId, Double howManyToTransact){
        logger.info("Making transaction for user: {} to card: {}", userId, whereToTransact);

        Optional<Card> cardToTransact = cardRepository.findByCardNumberAndUserUserId(whereToTransact, userId);

        if (cardToTransact.isPresent()){
            Card card = cardRepository.findByCardNumberAndExpireDate(
                    MaskCard.makeMaskOfCardNumber(cardDto.getCardNumber()),
                    cardDto.getExpirationDate()).orElseThrow(() -> new NotFound("Card not found"));
            if (bankUtil.checkCardBalance(card).getBalance().floatValue() < howManyToTransact){
                throw new NotEnoughMoney("Not Enough money to transact");
            }
            bankUtil.makeTransaction(card, whereToTransact, howManyToTransact);
            //bankUtil.checkCardBalance(cardToTransact.get());//Эта строчка в итоговом проекте должна заменять нижнюю
            cardToTransact.get().setBalance(cardToTransact.get().getBalance().
                    add(BigDecimal.valueOf(howManyToTransact))); // По сути этой строчки не должно быть, но из-за того что у нас нет связи с банком мы делаем это вручную
            cardRepository.save(card);
            cardRepository.save(cardToTransact.get());
            logger.info("Transaction completed successfully");
        }else {
            logger.error("Card with number {} not found for user {}", whereToTransact, userId);
            throw new NotFound("Card with this user as owner doesn't exist");
        }
    }
}
