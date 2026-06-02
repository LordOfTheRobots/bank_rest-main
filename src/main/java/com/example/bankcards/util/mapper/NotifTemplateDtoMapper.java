package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.NotificationTemplateDto;
import com.example.bankcards.entity.NotificationTemplateText;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("templateDtoMapper")
public class NotifTemplateDtoMapper implements DtoMapper<NotificationTemplateDto, NotificationTemplateText>{
    @Override
    public NotificationTemplateDto map(NotificationTemplateText t) {
        return new NotificationTemplateDto(
                t.getId(), t.getType().getCode(), t.getLocale(), t.getTitle(), t.getBody()
        );
    }
}
