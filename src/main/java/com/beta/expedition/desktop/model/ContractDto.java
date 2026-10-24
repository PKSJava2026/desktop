package com.beta.expedition.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ContractDto(
        Long id,
        ContractKind kind,
        Long orderId,
        OrderStatus orderStatus,
        Long counterpartyId,
        String counterpartyNickname,
        Long forwarderId,
        String forwarderNickname,
        BigDecimal price,
        String terms,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus status,
        OffsetDateTime createdAt) {
}
