package com.example.bankcards.dto;

import jakarta.annotation.Nullable;

public record NotificationTemplatePatchDto(
        @Nullable String title,
        @Nullable String body,
        @Nullable String locale
) {}
