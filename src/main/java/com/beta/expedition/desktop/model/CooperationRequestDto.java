package com.beta.expedition.desktop.model;

import java.time.OffsetDateTime;

public record CooperationRequestDto(
        Long id,
        Long carrierId,
        String carrierNickname,
        String description,
        String vehicleInfo,
        CooperationStatus status,
        OffsetDateTime createdAt) {
}
