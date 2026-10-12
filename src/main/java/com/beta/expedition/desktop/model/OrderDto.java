package com.beta.expedition.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OrderDto(
        Long id,
        Long customerId,
        String customerNickname,
        String cargoDescription,
        BigDecimal weightKg,
        BigDecimal volumeM3,
        String origin,
        String destination,
        LocalDate desiredDate,
        OrderStatus status,
        OffsetDateTime createdAt) {
}
