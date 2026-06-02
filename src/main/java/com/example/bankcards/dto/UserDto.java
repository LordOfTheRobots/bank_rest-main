package com.example.bankcards.dto;

import com.example.bankcards.entity.NotificationType;
import com.example.bankcards.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID userId;
    private String email;
    private String password;
    private String telephoneNumber;
    private String telegramId;
    private String roleName;
    private List<String> notificationNames;
    @JsonIgnore
    private Role role;
    @JsonIgnore
    private List<NotificationType> notificationTypes;
}
