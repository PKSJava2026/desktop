package com.beta.expedition.desktop.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderRequest(
        String cargoDescription,
        BigDecimal weightKg,
        BigDecimal volumeM3,
        String origin,
        String destination,
        LocalDate desiredDate) {
}
