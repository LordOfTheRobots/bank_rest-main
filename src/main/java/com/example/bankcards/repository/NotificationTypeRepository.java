package com.example.bankcards.repository;

import com.example.bankcards.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, String> {
    @Query("SELECT DISTINCT t FROM NotificationType t " +
            "LEFT JOIN FETCH t.templates " +
            "LEFT JOIN FETCH t.supportedChannels " +
            "WHERE t.code = :code")
    Optional<NotificationType> findWithTemplatesByCode(@Param("code") String code);
    List<NotificationType> findAllByCodeIn(Collection<String> codes);
    @Query("SELECT t FROM NotificationType t LEFT JOIN FETCH t.supportedChannels")
    List<NotificationType> findAllWithChannels();
}
