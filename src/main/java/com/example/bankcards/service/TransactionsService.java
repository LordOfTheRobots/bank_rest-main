package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.MyBankUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionsService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private MyBankUtil bankUtil;

    public void makeTransaction(CardEnteredDto card, String whereToTransact, UUID userId){
        Optional<Card> cardToTransact = cardRepository.findByCardNumberAndUserId(whereToTransact, userId);
        if (cardToTransact.isPresent()){
            bankUtil.makeTransaction(cardRepository.findByCardNumberAndExpireDate(
                            MaskCard.makeMaskOfCardNumber(card.getCardNumber()),
                            card.getExpirationDate()).orElseThrow(() -> new NotFound("Card not found")),
                    whereToTransact);
        }else {
            throw new NotFound("Card with this user as owner doesn't exist");
        }
    }
}
