package com.example.bankcards.exception;

public class TransactedMoneyIsNegativeOrZero extends RuntimeException {
    public TransactedMoneyIsNegativeOrZero(String message) {
        super(message);
    }
}
