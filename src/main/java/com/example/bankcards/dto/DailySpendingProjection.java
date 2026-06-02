package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public interface DailySpendingProjection {
    LocalDate getDate();
    BigDecimal getAmount();
}
