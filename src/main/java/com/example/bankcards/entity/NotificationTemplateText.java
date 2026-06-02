package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "notification_templates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template_type_channel_locale",
                columnNames = {"type_code", "locale"}
        ))
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NotificationTemplateText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code", nullable = false)
    private NotificationType type;

    @Column(name = "locale", nullable = false, length = 5)
    private String locale;

    @Column(name = "title")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;
}
