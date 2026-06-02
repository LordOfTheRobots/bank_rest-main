package com.example.bankcards.service;

import com.example.bankcards.config.NotificationProperties;
import com.example.bankcards.event.Notification;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final NotificationProperties properties;

    @Scheduled(cron = "${scheduler.analytics.cron:0 0 15 * * ?}")
    @Transactional(readOnly = true)
    public void sendServiceReminders() {
        APP_LOG.info("Starting service reminders batch");
        try (Stream<User> userStream = userRepository.streamAllUsers()) {
            userStream.forEach(user -> {
                try {
                    boolean hasContact = user.getEmail() != null || user.getTelegramId() != null;
                    if (!hasContact) return;
                    Object[] payload = new Object[]{user};
                    publisher.publishEvent(new Notification(
                            user,
                            "SERVICE_REMINDER",
                            user.getLocal() != null ? user.getLocal() : properties.getDefaultLanguage(),
                            payload
                    ));
                } catch (Exception e) {
                    APP_LOG.error("Ошибка обработки пользователя {}: {}",
                            user.getUserId(), e.getMessage(), e);
                }
            });
        }
        APP_LOG.info("Service reminders batch finished");
    }
}