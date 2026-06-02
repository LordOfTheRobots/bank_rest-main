package com.example.bankcards.dto;

import com.example.bankcards.entity.NotificationTemplateText;

public record NotificationTemplateDto(
        Long id,
        String typeCode,
        String locale,
        String title,
        String body
){
    public static NotificationTemplateDto fromEntity(NotificationTemplateText t){
        return new NotificationTemplateDto(
                t.getId(), t.getType().getCode(), t.getLocale(), t.getTitle(), t.getBody()
        );
    }
}
