package com.example.bankcards.util.notifications.render;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Order(5)
public class SystemContextHandler extends ContextEnricher {
    @Override public boolean supports(Object p) { return true; }

    @Override public void enrich(Object p, Map<String, Object> ctx) {
        ctx.putIfAbsent("current_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        ctx.putIfAbsent("app_name", "BankApp");
    }
}