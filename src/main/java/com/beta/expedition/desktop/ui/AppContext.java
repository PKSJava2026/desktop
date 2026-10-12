package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.service.AuthService;
import com.beta.expedition.desktop.service.ContractService;
import com.beta.expedition.desktop.service.CooperationService;
import com.beta.expedition.desktop.service.NotificationService;
import com.beta.expedition.desktop.service.OrderService;
import com.beta.expedition.desktop.service.RatingService;
import com.beta.expedition.desktop.service.StatisticsService;
import com.beta.expedition.desktop.session.Session;

public final class AppContext {

    private final Api api;
    private final Session session;
    private final AuthService authService;
    private final OrderService orderService;
    private final ContractService contractService;
    private final CooperationService cooperationService;
    private final NotificationService notificationService;
    private final RatingService ratingService;
    private final StatisticsService statisticsService;

    public AppContext(Api api) {
        this.api = api;
        this.session = new Session();
        this.authService = new AuthService(api.auth(), session);
        this.orderService = new OrderService(api.orders(), session);
        this.contractService = new ContractService(api.contracts(), api.users(), orderService, session);
        this.cooperationService = new CooperationService(api.cooperation(), api.contracts(), orderService, session);
        this.notificationService = new NotificationService(api.notifications());
        this.ratingService = new RatingService(api.ratings(), session);
        this.statisticsService = new StatisticsService(api.orders(), api.reports());
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

    public ContractService contractService() {
        return contractService;
    }

    public CooperationService cooperationService() {
        return cooperationService;
    }

    public NotificationService notificationService() {
        return notificationService;
    }

    public RatingService ratingService() {
        return ratingService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }
}
