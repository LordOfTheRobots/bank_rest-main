package com.example.bankcards.exception;

public class RefreshTokenExpired extends RuntimeException {
    public RefreshTokenExpired(String message) {
        super(message);
    }
}
