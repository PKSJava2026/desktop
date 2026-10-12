package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderSort;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.OrderQuery;
import com.beta.expedition.desktop.model.request.OrderRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class MockOrderApi implements OrderApi {

    private final MockStore store;
    private final MockSession session;

    MockOrderApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public OrderDto create(OrderRequest request) {
        UserDto customer = session.require(Role.CUSTOMER);
        validate(request);
        return store.addOrder(customer.id(), request.cargoDescription().trim(), request.weightKg(),
                request.volumeM3(), request.origin().trim(), request.destination().trim(),
                request.desiredDate(), OrderStatus.NEW, OffsetDateTime.now());
    }

    @Override
    public List<OrderDto> mine() {
        UserDto customer = session.require(Role.CUSTOMER);
        return store.orders.values().stream()
                .filter(order -> order.customerId().equals(customer.id()))
                .toList();
    }

    @Override
    public OrderDto get(long id) {
        UserDto actor = session.require(Role.CUSTOMER, Role.FORWARDER, Role.ADMIN);
        OrderDto order = store.order(id);
        if (actor.role() == Role.CUSTOMER && !order.customerId().equals(actor.id())) {
            throw MockErrors.notFound("заявка", id);
        }
        return order;
    }

    @Override
    public OrderDto update(long id, OrderRequest request) {
        OrderDto order = own(id);
        requireNew(order, "изменить");
        validate(request);
        OrderDto updated = new OrderDto(order.id(), order.customerId(), order.customerNickname(),
                request.cargoDescription().trim(), request.weightKg(), request.volumeM3(),
                request.origin().trim(), request.destination().trim(), request.desiredDate(), order.status(),
                order.createdAt());
        store.orders.put(id, updated);
        return updated;
    }

    @Override
    public void cancel(long id) {
        store.setOrderStatus(own(id), OrderStatus.CANCELLED);
    }

    @Override
    public void delete(long id) {
        OrderDto order = own(id);
        requireNew(order, "удалить");
        boolean hasContracts = store.contracts.values().stream()
                .anyMatch(table -> table.values().stream().anyMatch(c -> c.orderId().equals(id)));
        if (hasContracts) {
            throw MockErrors.conflict("По заявке #" + id + " есть договоры, удалить её нельзя");
        }
        store.orders.remove(id);
    }

    @Override
    public List<OrderDto> search(OrderQuery query) {
        UserDto actor = session.require(Role.CUSTOMER, Role.FORWARDER, Role.ADMIN);
        OrderQuery q = query == null ? OrderQuery.all() : query;
        if (q.from() != null && q.to() != null && q.to().isBefore(q.from())) {
            throw MockErrors.invalid("Конец периода раньше его начала");
        }
        String needle = q.text() == null || q.text().isBlank() ? null : q.text().trim().toLowerCase(Locale.ROOT);
        List<OrderDto> found = store.orders.values().stream()
                .filter(order -> actor.role() != Role.CUSTOMER || order.customerId().equals(actor.id()))
                .filter(order -> needle == null || matches(order, needle))
                .filter(order -> q.status() == null || order.status() == q.status())
                .filter(order -> q.from() == null || !createdDate(order).isBefore(q.from()))
                .filter(order -> q.to() == null || !createdDate(order).isAfter(q.to()))
                .filter(order -> q.customerId() == null || order.customerId().equals(q.customerId()))
                .filter(order -> !q.activeOnly() || isActive(order))
                .toList();
        if (q.sort() == null) {
            return found;
        }
        return found.stream().sorted(comparator(q.sort(), q.descending())).toList();
    }

    @Override
    public void changeStatus(long id, OrderStatus status) {
        session.requireStaff();
        if (status == null) {
            throw MockErrors.invalid("Выберите новый статус заявки");
        }
        if (status == OrderStatus.CONTRACTED) {
            throw MockErrors.conflict("Статус «" + status + "» устанавливается подтверждением договора");
        }
        store.setOrderStatus(store.order(id), status);
    }

    private OrderDto own(long id) {
        UserDto customer = session.require(Role.CUSTOMER);
        OrderDto order = store.order(id);
        if (!order.customerId().equals(customer.id())) {
            throw MockErrors.notFound("заявка", id);
        }
        return order;
    }

    private void requireNew(OrderDto order, String action) {
        if (order.status() != OrderStatus.NEW) {
            throw MockErrors.conflict("Нельзя " + action + " заявку в статусе «" + order.status()
                    + "» (только «" + OrderStatus.NEW + "»)");
        }
    }

    private void validate(OrderRequest request) {
        if (request == null) {
            throw MockErrors.invalid("Заполните данные заявки");
        }
        if (isBlank(request.cargoDescription())) {
            throw MockErrors.invalid("Описание груза обязательно");
        }
        if (isBlank(request.origin()) || isBlank(request.destination())) {
            throw MockErrors.invalid("Пункты отправления и назначения обязательны");
        }
        if (request.origin().trim().equalsIgnoreCase(request.destination().trim())) {
            throw MockErrors.invalid("Пункты отправления и назначения не должны совпадать");
        }
        if (request.weightKg() != null && request.weightKg().signum() <= 0) {
            throw MockErrors.invalid("Вес должен быть больше нуля");
        }
        if (request.volumeM3() != null && request.volumeM3().signum() <= 0) {
            throw MockErrors.invalid("Объём должен быть больше нуля");
        }
        if (request.desiredDate() != null && request.desiredDate().isBefore(LocalDate.now())) {
            throw MockErrors.invalid("Желаемая дата не может быть в прошлом");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean matches(OrderDto order, String needle) {
        return order.cargoDescription().toLowerCase(Locale.ROOT).contains(needle)
                || order.origin().toLowerCase(Locale.ROOT).contains(needle)
                || order.destination().toLowerCase(Locale.ROOT).contains(needle);
    }

    private static boolean isActive(OrderDto order) {
        return order.status() != OrderStatus.DELIVERED && order.status() != OrderStatus.CANCELLED;
    }

    private static LocalDate createdDate(OrderDto order) {
        return order.createdAt().toLocalDate();
    }

    private static Comparator<OrderDto> comparator(OrderSort sort, boolean descending) {
        if (sort == OrderSort.WEIGHT) {
            Comparator<BigDecimal> weights = descending ? Comparator.reverseOrder()
                    : Comparator.naturalOrder();
            return Comparator.comparing(OrderDto::weightKg, Comparator.nullsLast(weights));
        }
        Comparator<OrderDto> ascending = sort == OrderSort.DATE
                ? Comparator.comparing(OrderDto::createdAt)
                : Comparator.comparing(OrderDto::status);
        return descending ? ascending.reversed() : ascending;
    }
}
