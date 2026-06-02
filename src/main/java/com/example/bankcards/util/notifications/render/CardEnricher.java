package com.example.bankcards.util.notifications.render;

import com.example.bankcards.entity.Card;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Map;

@Component
@Order(2)
public class CardEnricher extends ContextEnricher {
    @Override public boolean supports(Object p) { return p instanceof Card; }

    @Override public void enrich(Object p, Map<String, Object> ctx) {
        Card card = (Card) p;
        ctx.putIfAbsent("card_id", card.getCardId());
        ctx.putIfAbsent("card_number", card.getCardNumber());

        String num = card.getCardNumber();
        ctx.putIfAbsent("card_mask", num != null && num.length() > 4 ? "****" + num.substring(num.length() - 4) : "****0000");

        ctx.putIfAbsent("card_expire_date", card.getExpireDate());
        ctx.putIfAbsent("card_balance", card.getBalance() != null ? card.getBalance().setScale(2, RoundingMode.HALF_UP) : "0.00");
        ctx.putIfAbsent("card_bank_token", card.getBankToken());
    }
}
