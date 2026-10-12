package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.service.AuthService;
import com.beta.expedition.desktop.service.NotificationService;
import com.beta.expedition.desktop.service.OrderService;
import com.beta.expedition.desktop.service.RatingService;
import com.beta.expedition.desktop.session.Session;

public final class AppContext {

    private final Api api;
    private final Session session;
    private final AuthService authService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final RatingService ratingService;

    public AppContext(Api api) {
        this.api = api;
        this.session = new Session();
        this.authService = new AuthService(api.auth(), session);
        this.orderService = new OrderService(api.orders(), session);
        this.notificationService = new NotificationService(api.notifications());
        this.ratingService = new RatingService(api.ratings(), session);
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

    public NotificationService notificationService() {
        return notificationService;
    }

    public RatingService ratingService() {
        return ratingService;
    }
}
