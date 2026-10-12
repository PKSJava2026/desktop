package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

public record OrderFilter(String text, OrderStatus status, LocalDate from, LocalDate to) {

    public String problem() {
        if (from != null && to != null && to.isBefore(from)) {
            return "Период: дата «По» не может быть раньше даты «С».";
        }
        return null;
    }

    public boolean matches(OrderDto order) {
        if (status != null && order.status() != status) {
            return false;
        }
        LocalDate created = order.createdAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        if (from != null && created.isBefore(from)) {
            return false;
        }
        if (to != null && created.isAfter(to)) {
            return false;
        }
        return containsText(order);
    }

    private boolean containsText(OrderDto order) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String needle = text.trim().toLowerCase(Locale.ROOT);
        return contains(order.cargoDescription(), needle)
                || contains(order.origin(), needle)
                || contains(order.destination(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
