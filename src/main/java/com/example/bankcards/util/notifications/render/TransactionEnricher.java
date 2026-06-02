package com.example.bankcards.util.notifications.render;

import com.example.bankcards.entity.Transaction;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Order(1)
public class TransactionEnricher extends ContextEnricher {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override public boolean supports(Object p) { return p instanceof Transaction; }

    @Override public void enrich(Object p, Map<String, Object> ctx) {
        Transaction tx = (Transaction) p;
        ctx.putIfAbsent("tx_id", tx.getTransactionId());
        ctx.putIfAbsent("tx_amount", tx.getAmount() != null ? tx.getAmount().setScale(2, RoundingMode.HALF_UP) : "0.00");
        ctx.putIfAbsent("tx_date", tx.getTransactionDate() != null ? tx.getTransactionDate().toLocalDateTime().format(DATE_FMT) : "");
        ctx.putIfAbsent("tx_description", tx.getDescription() != null ? tx.getDescription() : "Операция");
        ctx.putIfAbsent("tx_to_account", tx.getSecondaryCard() != null ? tx.getSecondaryCard() : "");

    }
}