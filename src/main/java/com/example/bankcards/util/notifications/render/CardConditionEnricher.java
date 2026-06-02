package com.example.bankcards.util.notifications.render;

import com.example.bankcards.entity.CardCondition;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Order(3)
public class CardConditionEnricher extends ContextEnricher {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override public boolean supports(Object p) { return p instanceof CardCondition; }

    @Override public void enrich(Object p, Map<String, Object> ctx) {
        CardCondition cond = (CardCondition) p;
        ctx.putIfAbsent("condition_id", cond.getConditionId());
        ctx.putIfAbsent("condition_comment", cond.getComment() != null ? cond.getComment() : "");
        ctx.putIfAbsent("condition_date", cond.getDateOfCondition() != null ? cond.getDateOfCondition().toLocalDateTime().format(DATE_FMT) : "");

        if (cond.getConditionName() != null) {
            ctx.putIfAbsent("condition_code", cond.getConditionName().getCode());
            ctx.putIfAbsent("condition_label", cond.getConditionName().getLabel());
            ctx.putIfAbsent("condition_color", cond.getConditionName().getColor());
            ctx.putIfAbsent("condition_available", cond.getConditionName().getIsAvailable());
        }
    }
}
