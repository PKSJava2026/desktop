package com.beta.expedition.desktop.session;

import com.beta.expedition.desktop.model.UserDto;

public class Session {

    private UserDto user;

    public void start(UserDto user) {
        this.user = user;
    }

    public void clear() {
        this.user = null;
    }

    public boolean isAuthenticated() {
        return user != null;
    }

    public UserDto user() {
        if (user == null) {
            throw new IllegalStateException("Пользователь не вошёл в систему");
        }
        return user;
    }
}
