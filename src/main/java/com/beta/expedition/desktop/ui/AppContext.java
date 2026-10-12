package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.service.AuthService;
import com.beta.expedition.desktop.service.OrderService;
import com.beta.expedition.desktop.session.Session;

public final class AppContext {

    private final Api api;
    private final Session session;
    private final AuthService authService;
    private final OrderService orderService;

    public AppContext(Api api) {
        this.api = api;
        this.session = new Session();
        this.authService = new AuthService(api.auth(), session);
        this.orderService = new OrderService(api.orders(), session);
    }

    public Api api() {
        return api;
    }

    public Session session() {
        return session;
    }

    public AuthService authService() {
        return authService;
    }

    public OrderService orderService() {
        return orderService;
    }
}
