package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class MaskCard {
    public static String makeMaskOfCardNumber(String cardNumber){
        return "*".repeat(cardNumber.length() - 4) +
                cardNumber.substring(cardNumber.length() - 4);
    }
}
