package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.RatingApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.RatingsOverview;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.request.RatingRequest;
import com.beta.expedition.desktop.session.Session;

import java.util.List;

public class RatingService {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;
    public static final int MAX_COMMENT_LENGTH = 500;

    private final RatingApi api;
    private final Session session;

    public RatingService(RatingApi api, Session session) {
        this.api = api;
        this.session = session;
    }

    public boolean ratesForwarder() {
        return session.user().role() == Role.CUSTOMER;
    }

    public String targetTitle() {
        return ratesForwarder() ? "экспедитора" : "перевозчика";
    }

    public List<OrderDto> unratedOrders() {
        return api.unratedOrders();
    }

    public RatingDto rate(long orderId, int score, String comment) {
        return api.rate(orderId, new RatingRequest(score, comment));
    }

    public RatingsOverview mine() {
        return api.mine();
    }
}
