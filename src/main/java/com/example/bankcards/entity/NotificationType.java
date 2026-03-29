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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private MessageType name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_required")
    private Boolean isRequired;

    @ManyToMany(mappedBy = "notificationTypes")
    private List<User> users;
}
