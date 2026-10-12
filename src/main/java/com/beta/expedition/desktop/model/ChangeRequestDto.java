package com.beta.expedition.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ChangeRequestDto(
        Long id,
        ContractKind kind,
        Long contractId,
        ChangeRequestType type,
        Long initiatedBy,
        String initiatorNickname,
        BigDecimal proposedPrice,
        String proposedTerms,
        LocalDate proposedStartDate,
        LocalDate proposedEndDate,
        ChangeRequestStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime resolvedAt) {
}
