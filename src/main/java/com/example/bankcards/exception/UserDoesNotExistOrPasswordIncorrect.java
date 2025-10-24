package com.example.bankcards.exception;

public class UserDoesNotExistOrPasswordIncorrect extends RuntimeException {
    public UserDoesNotExistOrPasswordIncorrect(String message) {
        super(message);
    }
}
