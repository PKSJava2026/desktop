package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.request.OrderQuery;
import com.beta.expedition.desktop.model.request.OrderRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

final class HttpOrderApi implements OrderApi {

    private static final TypeReference<List<OrderDto>> ORDERS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpOrderApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public OrderDto create(OrderRequest request) {
        return transport.post("/api/orders", request, OrderDto.class);
    }

    @Override
    public List<OrderDto> mine() {
        return transport.get("/api/orders/my", ORDERS);
    }

    @Override
    public OrderDto get(long id) {
        return transport.get("/api/orders/" + id, OrderDto.class);
    }

    @Override
    public OrderDto update(long id, OrderRequest request) {
        return transport.put("/api/orders/" + id, request, OrderDto.class);
    }

    @Override
    public void cancel(long id) {
        transport.post("/api/orders/" + id + "/cancel", null, Void.class);
    }

    @Override
    public void delete(long id) {
        transport.delete("/api/orders/" + id);
    }

    @Override
    public List<OrderDto> search(OrderQuery query) {
        Query q = new Query()
                .add("q", query.text())
                .add("status", query.status())
                .add("from", query.from())
                .add("to", query.to())
                .add("customerId", query.customerId())
                .add("active", query.activeOnly() ? "true" : null)
                .add("sort", query.sort())
                .add("desc", query.descending() ? "true" : null);
        return transport.get("/api/orders", q, ORDERS);
    }

    @Override
    public void changeStatus(long id, OrderStatus status) {
        transport.post("/api/orders/" + id + "/status", Map.of("status", status), Void.class);
    }
}
