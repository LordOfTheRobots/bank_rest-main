package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class UserAuthDto {
    private UUID userId;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
