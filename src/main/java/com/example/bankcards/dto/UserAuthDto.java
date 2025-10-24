package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Data
public class UserAuthDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
