package com.example.bankcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "spring.app.notifications")
public class NotificationProperties {
    private List<String> defaultTypes = List.of("WELCOME", "SERVICE_REMINDER");
    private String defaultLanguage = "en";
    private boolean enabled = true;
}
