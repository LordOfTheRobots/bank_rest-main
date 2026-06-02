package com.example.bankcards.util.notifications;

import com.example.bankcards.entity.DeliveryChannel;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserContactExtractor {

    public String resolveAddress(User user, DeliveryChannel channel) {
        return switch (channel) {
            case EMAIL    -> user.getEmail();
            case TELEGRAM -> user.getTelegramId();
        };
    }
}
