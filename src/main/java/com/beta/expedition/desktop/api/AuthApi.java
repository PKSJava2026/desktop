package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.AuthResult;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.LoginRequest;
import com.beta.expedition.desktop.model.request.RegisterRequest;

public interface AuthApi {

    UserDto register(RegisterRequest request);

    AuthResult login(LoginRequest request);

    void logout();
}
