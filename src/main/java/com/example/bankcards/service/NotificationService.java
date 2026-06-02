package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.DeliveryChannel;
import com.example.bankcards.entity.NotificationTemplateText;
import com.example.bankcards.entity.NotificationType;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.NotificationTemplateRepository;
import com.example.bankcards.repository.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationTypeRepository typeRepo;
    private final NotificationTemplateRepository templateRepo;

    public List<NotificationTypeDto> getAllTypes() {
        return typeRepo.findAllWithChannels().stream()
                .map(type -> new NotificationTypeDto(
                        type.getCode(),
                        type.getSupportedChannels() != null ?
                                type.getSupportedChannels().stream().map(Enum::name).toList() : List.of(),
                        type.getTemplates() != null ? type.getTemplates().size() : 0
                ))
                .toList();
    }

    public List<NotificationTemplateDto> getAllTemplates() {
        return templateRepo.findAllDto();
    }

    public List<NotificationTemplateDto> getTemplatesByType(String typeCode) {
        return templateRepo.findByTypeCodeDto(typeCode);
    }

    @Transactional
    public NotificationTypeDto createType(NotificationTypeCommand cmd) {
        NotificationType type = NotificationType.builder().code(cmd.code()).supportedChannels(parseChannels(cmd.supportedChannels())).templates(List.of()).build();
        return NotificationTypeDto.fromEntity(typeRepo.save(type));
    }

    @Transactional
    public NotificationTypeDto updateType(String code, NotificationTypeCommand cmd) {
        NotificationType type = typeRepo.findById(code).orElseThrow(() -> new IllegalArgumentException("Type not found: " + code));
        type.setSupportedChannels(parseChannels(cmd.supportedChannels()));
        return NotificationTypeDto.fromEntity(typeRepo.save(type));
    }

    @Transactional
    public void deleteType(String code) { typeRepo.deleteById(code); }

    @Transactional
    public NotificationTemplateDto createTemplate(NotificationTemplateCommand cmd) {
        NotificationTemplateText t = NotificationTemplateText.builder().
                type(typeRepo.findWithTemplatesByCode(cmd.typeCode()).orElseThrow(() -> new NotFound("No such notif types"))).
                locale(cmd.locale()).
                title(cmd.title()).
                body(cmd.body()).
                build();
        return NotificationTemplateDto.fromEntity(templateRepo.save(t));
    }

    @Transactional
    public NotificationTemplateDto updateTemplate(Long id, NotificationTemplateCommand cmd) {
        NotificationTemplateText t = templateRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));
        t.setType(typeRepo.findWithTemplatesByCode(cmd.typeCode()).orElseThrow(() -> new NotFound("No such notif types")));
        t.setLocale(cmd.locale());
        t.setTitle(cmd.title());
        t.setBody(cmd.body());
        return NotificationTemplateDto.fromEntity(templateRepo.save(t));
    }

    @Transactional
    public void deleteTemplate(Long id) { templateRepo.deleteById(id); }

    private List<DeliveryChannel> parseChannels(List<String> channels) {
        return channels.stream().map(String::toUpperCase).map(DeliveryChannel::valueOf).toList();
    }

    @Transactional
    public NotificationTypeDto patchType(String code, NotificationTypePatchDto dto) {
        NotificationType type = typeRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Тип не найден: " + code));

        if (dto.supportedChannels() != null) {
            type.setSupportedChannels(dto.supportedChannels().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::toUpperCase)
                    .map(DeliveryChannel::valueOf)
                    .toList());
        }
        return NotificationTypeDto.fromEntity(typeRepo.save(type));
    }

    @Transactional
    public NotificationTemplateDto patchTemplate(Long id, NotificationTemplatePatchDto dto) {
        NotificationTemplateText tpl = templateRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Шаблон не найден: " + id));

        if (dto.title() != null) tpl.setTitle(dto.title());
        if (dto.body() != null) tpl.setBody(dto.body());
        if (dto.locale() != null) tpl.setLocale(dto.locale());

        return NotificationTemplateDto.fromEntity(templateRepo.save(tpl));
    }
}