package com.example.bankcards.dto;

import java.time.LocalDate;
import java.util.List;

public record CardSpendingByDays(
        Long cardId,
        LocalDate startDate,
        LocalDate endDate,
        List<DailySpendingDto> days
) {}
