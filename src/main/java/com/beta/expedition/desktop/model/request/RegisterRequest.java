package com.beta.expedition.desktop.model.request;

import com.beta.expedition.desktop.model.Role;

public record RegisterRequest(
        String nickname,
        String email,
        String password,
        Role role) {
}
