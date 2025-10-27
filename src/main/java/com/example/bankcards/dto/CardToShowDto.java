package com.example.bankcards.dto;

import com.example.bankcards.entity.CardCondition;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class CardToShowDto {

    private Long cardId;

    private String cardMask;

    private String expireDate;

    private BigDecimal balance;

    private CardCondition cardCondition;
}
