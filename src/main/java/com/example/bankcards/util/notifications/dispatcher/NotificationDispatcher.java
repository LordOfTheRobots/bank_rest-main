package com.example.bankcards.util.notifications.dispatcher;

import com.example.bankcards.config.NotificationProperties;
import com.example.bankcards.entity.NotificationTemplateText;
import com.example.bankcards.event.Notification;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.NotificationTemplateRepository;
import com.example.bankcards.util.notifications.UserContactExtractor;
import com.example.bankcards.util.notifications.channel.NotificationChannel;
import com.example.bankcards.util.notifications.render.NotificationContextBuilder;
import com.example.bankcards.util.notifications.render.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");

    private final List<NotificationChannel> channels;
    private final NotificationTemplateRepository templateRepository;
    private final UserContactExtractor contactExtractor;
    private final NotificationContextBuilder contextBuilder;
    private final TemplateRenderer templateRenderer;
    private final NotificationProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(Notification event) {
        String typeCode = event.typeCode();
        String userId = event.user().getUserId().toString();
        APP_LOG.info("[NOTIF] Processing event: type={}, userId={}", typeCode, userId);

        try {
            NotificationTemplateText template = templateRepository
                    .findByTypeCodeAndLocaleWithFetch(typeCode, event.user().getLocal())
                    .orElseGet(() -> {
                        APP_LOG.warn("[NOTIF] Template for locale '{}' not found, falling back to '{}'",
                                event.user().getLocal(), properties.getDefaultLanguage());
                        return templateRepository.findByTypeCodeAndLocaleWithFetch(typeCode, properties.getDefaultLanguage())
                                .orElseThrow(() -> new NotFound("Template not found for type: " + typeCode));
                    });
            APP_LOG.debug("[NOTIF] Template loaded successfully: {}", template.getTitle());

            Map<String, Object> context = contextBuilder.build(event.entities());
            context.putIfAbsent("user", event.user());
            APP_LOG.debug("[NOTIF] Context built with {} keys", context.size());

            String title = templateRenderer.render(template.getTitle(), context);
            String body  = templateRenderer.render(template.getBody(), context);
            APP_LOG.debug("[NOTIF] Message rendered. Title length: {}, Body length: {}", title.length(), body.length());

            for (NotificationChannel channel : channels) {
                try {
                    String address = contactExtractor.resolveAddress(event.user(), channel.getChannelType());
                    if (address == null) {
                        APP_LOG.warn("[NOTIF] No address found for channel: {}", channel.getChannelType());
                        continue;
                    }
                    channel.send(address, title, body);
                    APP_LOG.info("[NOTIF] Sent {} to {} via {}", typeCode, address, channel.getChannelType());
                } catch (Exception e) {
                    APP_LOG.error("[NOTIF] Failed to send via {}: {}", channel.getChannelType(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            APP_LOG.error("[NOTIF] Notification pipeline failed for event {}: {}", typeCode, e.getMessage(), e);
        }
    }
}