package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.ApiException;

final class MockErrors {

    private MockErrors() {
    }

    static ApiException invalid(String message) {
        return new ApiException(400, message);
    }

    static ApiException unauthorized() {
        return unauthorized("Необходимо войти в систему");
    }

    static ApiException unauthorized(String message) {
        return new ApiException(401, message);
    }

    static ApiException forbidden() {
        return forbidden("Недостаточно прав для этого действия");
    }

    static ApiException forbidden(String message) {
        return new ApiException(403, message);
    }

    static ApiException notFound(String what, long id) {
        return new ApiException(404, "Не найдено: " + what + " #" + id);
    }

    static ApiException conflict(String message) {
        return new ApiException(409, message);
    }
}
