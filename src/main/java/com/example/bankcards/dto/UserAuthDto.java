package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class UserAuthDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
