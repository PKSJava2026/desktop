package com.beta.expedition.desktop.model;

import java.time.OffsetDateTime;

public record RatingDto(
        Long id,
        Long orderId,
        Long fromUserId,
        String fromNickname,
        Long toUserId,
        int score,
        String comment,
        OffsetDateTime createdAt) {
}
