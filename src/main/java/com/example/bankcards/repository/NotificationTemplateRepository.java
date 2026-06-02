package com.example.bankcards.repository;

import com.example.bankcards.dto.NotificationTemplateDto;
import com.example.bankcards.entity.NotificationTemplateText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateText, Long> {
    @Query("SELECT t FROM NotificationTemplateText t JOIN FETCH t.type WHERE t.type.code = :typeCode AND t.locale = :locale")
    Optional<NotificationTemplateText> findByTypeCodeAndLocaleWithFetch(
            @Param("typeCode") String typeCode,
            @Param("locale") String locale
    );
    @Query("SELECT new com.example.bankcards.dto.NotificationTemplateDto(t.id, t.type.code, t.locale, t.title, t.body) FROM NotificationTemplateText t")
    List<NotificationTemplateDto> findAllDto();

    @Query("SELECT new com.example.bankcards.dto.NotificationTemplateDto(t.id, t.type.code, t.locale, t.title, t.body) FROM NotificationTemplateText t WHERE t.type.code = :typeCode")
    List<NotificationTemplateDto> findByTypeCodeDto(@Param("typeCode") String typeCode);
}
