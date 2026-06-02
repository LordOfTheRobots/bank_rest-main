package com.example.bankcards.dto;

import java.util.List;
import java.util.Objects;

public record NotificationTypeDto(
        String code,
        List<String> supportedChannels,
        int templateCount
) {
    public static NotificationTypeDto fromEntity(com.example.bankcards.entity.NotificationType type) {
        List<String> channels = type.getSupportedChannels() != null
                ? type.getSupportedChannels().stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .toList()
                : List.of();

        return new NotificationTypeDto(
                type.getCode(),
                channels,
                type.getTemplates() != null ? type.getTemplates().size() : 0
        );
    }
}
