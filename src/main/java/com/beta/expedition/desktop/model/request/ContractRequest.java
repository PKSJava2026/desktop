package com.beta.expedition.desktop.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractRequest(
        Long orderId,
        Long counterpartyId,
        BigDecimal price,
        String terms,
        LocalDate startDate,
        LocalDate endDate) {
}
