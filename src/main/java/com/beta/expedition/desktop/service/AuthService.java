package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.AuthApi;
import com.beta.expedition.desktop.model.AuthResult;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.LoginRequest;
import com.beta.expedition.desktop.model.request.RegisterRequest;
import com.beta.expedition.desktop.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern NICKNAME = Pattern.compile("[\\p{L}\\p{N}_.-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final Set<Role> SELF_REGISTRATION_ROLES = EnumSet.of(Role.CUSTOMER, Role.CARRIER);

    private final AuthApi authApi;
    private final Session session;

    public AuthService(AuthApi authApi, Session session) {
        this.authApi = authApi;
        this.session = session;
    }

    public UserDto login(String nickname, String password) {
        if (isBlank(nickname)) {
            throw new ValidationException("Никнейм: введите никнейм");
        }
        if (isBlank(password)) {
            throw new ValidationException("Пароль: введите пароль");
        }
        AuthResult result = authApi.login(new LoginRequest(nickname.trim(), password));
        session.start(result.user());
        return result.user();
    }

    public UserDto register(String nickname, String email, String password, String confirmation, Role role) {
        validateRegistration(nickname, email, password, confirmation, role);
        authApi.register(new RegisterRequest(nickname.trim(), email.trim(), password, role));
        return login(nickname, password);
    }

    public void logout() {
        authApi.logout();
        session.clear();
    }

    private void validateRegistration(String nickname, String email, String password, String confirmation,
                                      Role role) {
        if (isBlank(nickname) || !NICKNAME.matcher(nickname.trim()).matches()) {
            throw new ValidationException("Никнейм: от 3 до 30 символов, буквы, цифры, «_», «.» и «-»");
        }
        if (isBlank(email) || email.trim().length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException("Email: укажите адрес в формате name@example.com");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new ValidationException("Пароль: не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        if (!password.equals(confirmation)) {
            throw new ValidationException("Подтверждение пароля: пароли не совпадают");
        }
        if (role == null || !SELF_REGISTRATION_ROLES.contains(role)) {
            throw new ValidationException("Роль: выберите заказчика или перевозчика");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
