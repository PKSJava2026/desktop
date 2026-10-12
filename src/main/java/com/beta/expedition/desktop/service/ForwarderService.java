package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.UserApi;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.ForwarderRequest;
import com.beta.expedition.desktop.ui.Validator;

import java.util.List;
import java.util.regex.Pattern;

public class ForwarderService {

    private static final Pattern NICKNAME = Pattern.compile("[\\p{L}\\p{N}_.-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 72;

    private final UserApi users;

    public ForwarderService(UserApi users) {
        this.users = users;
    }

    public List<UserDto> list() {
        return users.forwarders();
    }

    public UserDto create(ForwarderRequest request) {
        validate(request, request.password());
        return users.createForwarder(new ForwarderRequest(request.nickname().trim(), request.email().trim(),
                request.password()));
    }

    public void remove(long id) {
        users.removeForwarder(id);
    }

    public void validate(ForwarderRequest request, String confirmation) {
        String password = request.password();
        Validator validator = new Validator()
                .required("Никнейм", request.nickname())
                .matches("Никнейм", request.nickname(), NICKNAME,
                        "от 3 до 30 символов: буквы, цифры, «_», «.» и «-».")
                .required("Email", request.email())
                .length("Email", request.email(), 1, MAX_EMAIL_LENGTH)
                .matches("Email", request.email(), EMAIL, "укажите адрес в формате name@example.com.")
                .required("Пароль", password)
                .check(password == null || password.isBlank() || (password.length() >= MIN_PASSWORD_LENGTH
                        && password.length() <= MAX_PASSWORD_LENGTH),
                        "Поле «Пароль»: от " + MIN_PASSWORD_LENGTH + " до " + MAX_PASSWORD_LENGTH + " символов.")
                .check(password == null || password.equals(confirmation),
                        "Поле «Подтверждение пароля»: пароли не совпадают.");
        if (!validator.isValid()) {
            throw new ValidationException(validator.message());
        }
    }
}
