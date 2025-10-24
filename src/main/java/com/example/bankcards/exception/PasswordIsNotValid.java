package com.example.bankcards.exception;

public class PasswordIsNotValid extends RuntimeException {
    public PasswordIsNotValid(String message) {
        super(message);
    }
}
