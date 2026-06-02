package com.example.bankcards.util.notifications.render;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationContextBuilder {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private final List<ContextEnricher> enrichers;

    public Map<String, Object> build(Object... payloads) {
        Map<String, Object> context = new HashMap<>();
        if (payloads == null || payloads.length == 0) {
            APP_LOG.warn("[NOTIF] build() called with empty payloads");
            return context;
        }

        for (Object payload : payloads) {
            if (payload == null) continue;
            boolean enriched = false;
            for (ContextEnricher enricher : enrichers) {
                try {
                    if (enricher.supports(payload)) {
                        enricher.enrich(payload, context);
                        enriched = true;
                        APP_LOG.debug("[NOTIF] Payload enriched by: {}", enricher.getClass().getSimpleName());
                    }
                } catch (Exception e) {
                    APP_LOG.error("[NOTIF] Enricher {} failed for payload {}: {}",
                            enricher.getClass().getSimpleName(), payload.getClass(), e.getMessage(), e);
                }
            }
            if (!enriched) {
                APP_LOG.warn("[NOTIF] No enricher found for payload type: {}", payload.getClass().getName());
            }
        }
        return context;
    }
}
