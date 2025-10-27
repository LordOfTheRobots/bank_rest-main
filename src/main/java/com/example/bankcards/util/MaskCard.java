package com.example.bankcards.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MaskCard {
    private static final Logger logger = LoggerFactory.getLogger(MaskCard.class);

    public static String makeMaskOfCardNumber(String cardNumber){
        logger.debug("Masking card number of length: {}", cardNumber.length());
        String maskedCard = "*".repeat(cardNumber.length() - 4) +
                cardNumber.substring(cardNumber.length() - 4);
        logger.debug("Card number masked successfully");
        return maskedCard;
    }
}
