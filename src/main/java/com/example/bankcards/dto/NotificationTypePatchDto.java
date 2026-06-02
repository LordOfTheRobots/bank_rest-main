package com.example.bankcards.dto;

import jakarta.annotation.Nullable;
import java.util.List;

public record NotificationTypePatchDto(
        @Nullable List<String> supportedChannels
) {}
