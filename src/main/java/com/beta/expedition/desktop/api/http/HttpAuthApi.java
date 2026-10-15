package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.AuthApi;
import com.beta.expedition.desktop.model.AuthResult;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.LoginRequest;
import com.beta.expedition.desktop.model.request.RegisterRequest;

final class HttpAuthApi implements AuthApi {

    private final HttpTransport transport;

    HttpAuthApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        return transport.post("/api/auth/register", request, UserDto.class);
    }

    @Override
    public AuthResult login(LoginRequest request) {
        AuthResult result = transport.post("/api/auth/login", request, AuthResult.class);
        transport.setToken(result.token());
        return result;
    }

    @Override
    public void logout() {
        transport.clearToken();
    }
}
