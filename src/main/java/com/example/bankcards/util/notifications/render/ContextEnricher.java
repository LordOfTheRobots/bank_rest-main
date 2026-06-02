package com.example.bankcards.util.notifications.render;

import java.util.Map;


import java.util.Map;

public abstract class ContextEnricher {
    public abstract boolean supports(Object payload);

    public abstract void enrich(Object payload, Map<String, Object> context);
}