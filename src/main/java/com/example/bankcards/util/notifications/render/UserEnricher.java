package com.example.bankcards.util.notifications.render;

import com.example.bankcards.config.NotificationProperties;
import com.example.bankcards.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(4)
@RequiredArgsConstructor
public class UserEnricher extends ContextEnricher {
    private final NotificationProperties properties;

    @Override public boolean supports(Object p) { return p instanceof User; }

    @Override public void enrich(Object p, Map<String, Object> ctx) {
        User user = (User) p;
        ctx.putIfAbsent("user_id", user.getUserId());
        ctx.putIfAbsent("user_locale", user.getLocal() != null ? user.getLocal() : properties.getDefaultLanguage());
        ctx.putIfAbsent("user_email", user.getEmail());
        ctx.putIfAbsent("user_name", user.getEmail() != null ? user.getEmail().split("@")[0] : "Клиент");
        ctx.putIfAbsent("user_phone", user.getTelephoneNumber());
        ctx.putIfAbsent("user_telegram_id", user.getTelegramId());

        if (user.getRole() != null) {
            ctx.putIfAbsent("user_role", user.getRole().getRoleName());
        }
    }
}
