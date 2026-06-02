package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "notification_types")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NotificationType {
    @Id
    @Column(name = "type_code", nullable = false)
    private String code;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationTemplateText> templates;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "type_supported_channels", joinColumns = @JoinColumn(name = "type_code"))
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private List<DeliveryChannel> supportedChannels;
}
