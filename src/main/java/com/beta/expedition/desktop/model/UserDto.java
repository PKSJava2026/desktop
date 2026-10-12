package com.beta.expedition.desktop.model;

import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String nickname,
        String email,
        Role role,
        boolean active,
        OffsetDateTime createdAt) {
}
