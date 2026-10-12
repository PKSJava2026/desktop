package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.TableDto;
import com.beta.expedition.desktop.model.request.OrderQuery;
import com.beta.expedition.desktop.model.request.OrderRequest;
import com.beta.expedition.desktop.session.Session;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderService {

    private static final Map<OrderStatus, List<OrderStatus>> STAFF_TRANSITIONS = Map.of(
            OrderStatus.NEW, List.of(OrderStatus.CANCELLED),
            OrderStatus.CONTRACTED, List.of(OrderStatus.IN_TRANSIT, OrderStatus.CANCELLED),
            OrderStatus.IN_TRANSIT, List.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, List.of(),
            OrderStatus.CANCELLED, List.of());

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final OrderApi api;
    private final Session session;

    public OrderService(OrderApi api, Session session) {
        this.api = api;
        this.session = session;
    }

    public boolean isCustomer() {
        return session.user().role() == Role.CUSTOMER;
    }

    public List<OrderDto> list(boolean activeOnly) {
        if (isCustomer()) {
            return api.mine();
        }
        return api.search(new OrderQuery(null, null, null, null, null, activeOnly, null, false));
    }

    public OrderDto create(OrderRequest request) {
        return api.create(request);
    }

    public OrderDto update(long id, OrderRequest request) {
        return api.update(id, request);
    }

    public void cancel(long id) {
        api.cancel(id);
    }

    public void delete(long id) {
        api.delete(id);
    }

    public void changeStatus(long id, OrderStatus status) {
        api.changeStatus(id, status);
    }

    public TableDto exportTable(List<OrderDto> orders) {
        boolean withCustomer = !isCustomer();
        List<String> columns = new ArrayList<>();
        columns.add("№");
        if (withCustomer) {
            columns.add("Заказчик");
        }
        columns.addAll(List.of("Груз", "Откуда", "Куда", "Вес, кг", "Объём, м³", "Желаемая дата", "Статус",
                "Создана"));

        List<List<Object>> rows = new ArrayList<>();
        for (OrderDto order : orders) {
            List<Object> row = new ArrayList<>();
            row.add(order.id());
            if (withCustomer) {
                row.add(order.customerNickname());
            }
            row.add(order.cargoDescription());
            row.add(order.origin());
            row.add(order.destination());
            row.add(order.weightKg());
            row.add(order.volumeM3());
            row.add(formatDate(order.desiredDate()));
            row.add(order.status().toString());
            row.add(formatDateTime(order.createdAt()));
            rows.add(row);
        }
        return new TableDto("orders", columns, rows);
    }

    public boolean canEdit(OrderDto order) {
        return isCustomer() && order != null && order.status() == OrderStatus.NEW;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    private String formatDateTime(OffsetDateTime time) {
        return time == null ? "" : time.atZoneSameInstant(ZoneId.systemDefault()).format(DATE_TIME);
    }

    public boolean canDelete(OrderDto order) {
        return canEdit(order);
    }

    public boolean canCancel(OrderDto order) {
        return isCustomer() && order != null
                && (order.status() == OrderStatus.NEW || order.status() == OrderStatus.CONTRACTED);
    }

    public List<OrderStatus> nextStatuses(OrderDto order) {
        if (isCustomer() || order == null) {
            return List.of();
        }
        return STAFF_TRANSITIONS.get(order.status());
    }
}
