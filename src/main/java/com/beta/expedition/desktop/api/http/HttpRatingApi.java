package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.RatingApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.RatingSummaryDto;
import com.beta.expedition.desktop.model.RatingsOverview;
import com.beta.expedition.desktop.model.request.RatingRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

final class HttpRatingApi implements RatingApi {

    private static final TypeReference<List<OrderDto>> ORDERS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpRatingApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public RatingDto rate(long orderId, RatingRequest request) {
        return transport.post("/api/orders/" + orderId + "/ratings", request, RatingDto.class);
    }

    @Override
    public List<OrderDto> unratedOrders() {
        return transport.get("/api/orders/unrated", ORDERS);
    }

    @Override
    public RatingsOverview mine() {
        return transport.get("/api/ratings/my", RatingsOverview.class);
    }

    @Override
    public RatingSummaryDto overall() {
        return transport.get("/api/ratings/overall", RatingSummaryDto.class);
    }
}
