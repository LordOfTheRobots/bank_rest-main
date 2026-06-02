package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySpendingDto(
        LocalDate date,
        BigDecimal amount
) {}
