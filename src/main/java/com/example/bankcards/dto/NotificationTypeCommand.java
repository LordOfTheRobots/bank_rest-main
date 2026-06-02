package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record NotificationTypeCommand(
        @NotBlank String code,
        @NotNull List<String> supportedChannels
) {}
