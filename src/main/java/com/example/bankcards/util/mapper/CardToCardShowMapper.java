package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.entity.Card;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier("CardToCardShowMapper")
public class CardToCardShowMapper implements DtoMapper<CardToShowDto, Card> {
    @Override
    public CardToShowDto map(Card card) {
        return CardToShowDto.builder().
                cardId(card.getCardId()).
                cardMask(card.getCardNumber()).
                expireDate(card.getExpireDate()).
                balance(card.getBalance()).
                cardCondition(card.getCondition()).
                build();
    }
}
