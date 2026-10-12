package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import java.util.List;
import java.util.UUID;

final class MockSession {

    private static final Role[] STAFF = {Role.FORWARDER, Role.ADMIN};

    private final MockStore store;
    private Long userId;

    MockSession(MockStore store) {
        this.store = store;
    }

    String start(UserDto user) {
        userId = user.id();
        return UUID.randomUUID().toString();
    }

    void end() {
        userId = null;
    }

    UserDto current() {
        if (userId == null) {
            throw MockErrors.unauthorized();
        }
        UserDto user = store.users.get(userId);
        if (user == null || !user.active()) {
            end();
            throw MockErrors.unauthorized();
        }
        return user;
    }

    UserDto require(Role... roles) {
        UserDto user = current();
        if (!List.of(roles).contains(user.role())) {
            throw MockErrors.forbidden();
        }
        return user;
    }

    UserDto requireStaff() {
        return require(STAFF);
    }

    static boolean isStaff(UserDto user) {
        return user.role() == Role.FORWARDER || user.role() == Role.ADMIN;
    }
}
