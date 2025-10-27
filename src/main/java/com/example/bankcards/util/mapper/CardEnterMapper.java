package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.entity.Card;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("cardEnterMapper")
public class CardEnterMapper implements DtoMapper<Card, CardEnteredDto>{

    @Override
    public Card map(CardEnteredDto dto) {
        return Card.builder().
                cardNumber(dto.getCardNumber()).
                expireDate(dto.getExpirationDate()).
                build();
    }
}
