package com.example.bankcards.dto;
import jakarta.validation.constraints.NotBlank;

public record NotificationTemplateCommand(
        @NotBlank String typeCode,
        @NotBlank String locale,
        @NotBlank String title,
        @NotBlank String body
) {}
