package com.example.bankcards.dto;

import com.example.bankcards.entity.CardCondition;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class CardToShowDto {
    private String cardNumber;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
            message = "Expire date must be in format MM/YY")
    private String expireDate;

    private BigDecimal balance;

    private CardCondition cardCondition;
}
