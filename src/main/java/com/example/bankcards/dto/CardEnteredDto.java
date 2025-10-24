package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class CardEnteredDto {
    @NotBlank
    @Pattern(regexp = "^[0-9]{12,19}$", message = "Card number must contain 12 to 19 digits")
    private String cardNumber;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
            message = "Expire date must be in format MM/YY")
    @NotBlank
    private String expirationDate;

    private UUID userId;
}
