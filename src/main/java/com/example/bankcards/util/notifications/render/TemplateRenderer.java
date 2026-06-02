package com.example.bankcards.util.notifications.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([\\w]+)}");

    public String render(String template, Map<String, Object> context) {
        if (template == null) {
            APP_LOG.warn("[NOTIF] render() called with null template");
            return null;
        }
        if (context == null || context.isEmpty()) {
            APP_LOG.warn("[NOTIF] render() called with empty context. Placeholders will not be resolved.");
            return template;
        }

        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer result = new StringBuffer();
        boolean hasMissing = false;
        APP_LOG.debug("[NOTIF] Template: {}", template);
        APP_LOG.debug("[NOTIF] Context keys: {}", context.keySet());
        APP_LOG.debug("[NOTIF] Context['user_name'] = {}", context.get("user_name"));
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = context.get(key);
            if (value == null) {
                hasMissing = true;
                APP_LOG.warn("[NOTIF] Missing context key '{}' in template", key);
            }
            Object replacement = (value != null) ? value : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(replacement)));
        }
        matcher.appendTail(result);

        if (hasMissing) {
            APP_LOG.warn("[NOTIF] Template rendering completed with unresolved placeholders");
        }
        return result.toString();
    }
}