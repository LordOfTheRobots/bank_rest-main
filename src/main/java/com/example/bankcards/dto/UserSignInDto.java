package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserSignInDto {
    @Email
    private String email;
    private String password;
}
