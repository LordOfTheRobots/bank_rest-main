package com.example.bankcards.event;

import com.example.bankcards.entity.User;

import java.util.UUID;

public record Notification(
        User user,
        String typeCode,
        String locale,
        Object[] entities
) {}
