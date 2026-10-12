package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.AuthApi;
import com.beta.expedition.desktop.model.AuthResult;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.LoginRequest;
import com.beta.expedition.desktop.model.request.RegisterRequest;
import java.util.Set;

final class MockAuthApi implements AuthApi {

    private static final Set<Role> SELF_REGISTRATION_ROLES = Set.of(Role.CUSTOMER, Role.CARRIER);

    private final MockStore store;
    private final MockSession session;

    MockAuthApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        if (request == null) {
            throw MockErrors.invalid("Заполните данные регистрации");
        }
        if (!SELF_REGISTRATION_ROLES.contains(request.role())) {
            throw MockErrors.invalid("Эта роль недоступна для самостоятельной регистрации");
        }
        return store.registerUser(request.nickname(), request.email(), request.password(), request.role());
    }

    @Override
    public AuthResult login(LoginRequest request) {
        if (request == null || request.nickname() == null || request.password() == null) {
            throw MockErrors.invalid("Введите никнейм и пароль");
        }
        UserDto user = store.findByNickname(request.nickname());
        boolean valid = user != null && user.active() && request.password().equals(store.passwords.get(user.id()));
        if (!valid) {
            throw MockErrors.unauthorized("Неверный никнейм или пароль");
        }
        return new AuthResult(session.start(user), user);
    }

    @Override
    public void logout() {
        session.end();
    }
}
