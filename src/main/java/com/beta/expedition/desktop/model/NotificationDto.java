package com.beta.expedition.desktop.model;

import java.time.OffsetDateTime;

public record NotificationDto(
        Long id,
        String message,
        Long changeRequestId,
        boolean read,
        OffsetDateTime createdAt) {
}
