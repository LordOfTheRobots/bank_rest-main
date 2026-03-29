package com.example.bankcards.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardStatus {
    ACTIVE(1, "Активна", "green", true),
    BLOCKED(2, "Заблокирована", "red", false),
    EXPIRED(3, "Истек срок", "gray", false),
    PENDING(4, "На рассмотрении", "yellow", true);

    private final Integer code;
    private final String label;
    private final String color;
    private final Boolean isAvailable;

    public static CardStatus fromCode(int code) {
        for (CardStatus status : CardStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус карты: " + code);
    }
}
