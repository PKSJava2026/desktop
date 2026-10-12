package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.StatItem;
import com.beta.expedition.desktop.model.TableDto;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class MockReportApi implements ReportApi {

    private static final String BOM = "﻿";
    private static final String STATISTICS_SHEET = "Статистика";

    private final MockStore store;
    private final MockSession session;
    private final Map<String, Supplier<TableDto>> tables;

    MockReportApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
        this.tables = new LinkedHashMap<>();
        tables.put("users", this::usersTable);
        tables.put("orders", this::ordersTable);
        tables.put("cooperation_requests", this::cooperationTable);
        tables.put("customer_contracts", () -> contractsTable(ContractKind.CUSTOMER));
        tables.put("carrier_contracts", () -> contractsTable(ContractKind.CARRIER));
        tables.put("contract_change_requests", this::changeRequestsTable);
        tables.put("ratings", this::ratingsTable);
        tables.put("notifications", this::notificationsTable);
    }

    @Override
    public List<StatItem> statistics() {
        session.requireStaff();
        return buildStatistics();
    }

    @Override
    public List<String> tables() {
        session.requireStaff();
        return List.copyOf(tables.keySet());
    }

    @Override
    public TableDto table(String name) {
        session.requireStaff();
        Supplier<TableDto> table = name == null ? null : tables.get(name);
        if (table == null) {
            throw MockErrors.invalid("Неизвестная таблица: " + name);
        }
        return table.get();
    }

    @Override
    public byte[] exportXlsx() {
        session.requireStaff();
        List<TableDto> sheets = new ArrayList<>();
        sheets.add(statisticsTable());
        tables.values().forEach(table -> sheets.add(table.get()));
        return MockXlsxWriter.write(sheets);
    }

    @Override
    public byte[] exportCsv() {
        session.requireStaff();
        StringBuilder csv = new StringBuilder(BOM);
        appendCsv(csv, statisticsTable());
        tables.values().forEach(table -> appendCsv(csv.append("\r\n"), table.get()));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<StatItem> buildStatistics() {
        List<OrderDto> orders = List.copyOf(store.orders.values());
        List<ContractDto> customer = List.copyOf(store.contracts.get(ContractKind.CUSTOMER).values());
        List<ContractDto> carrier = List.copyOf(store.contracts.get(ContractKind.CARRIER).values());
        List<RatingDto> ratings = List.copyOf(store.ratings.values());
        double average = ratings.stream().mapToInt(RatingDto::score).average().orElse(0);
        String rating = ratings.isEmpty() ? "оценок пока нет"
                : String.format(Locale.forLanguageTag("ru"), "%.2f из 5 (оценок: %d)", average, ratings.size());
        return List.of(
                new StatItem("Заявок на перевозку (всего)", String.valueOf(orders.size())),
                new StatItem("Заявок в работе", String.valueOf(orders.stream()
                        .filter(o -> o.status() != OrderStatus.DELIVERED && o.status() != OrderStatus.CANCELLED)
                        .count())),
                new StatItem("Успешно выполнено заказов (доставлено)", String.valueOf(orders.stream()
                        .filter(o -> o.status() == OrderStatus.DELIVERED).count())),
                new StatItem("Отменено заявок", String.valueOf(orders.stream()
                        .filter(o -> o.status() == OrderStatus.CANCELLED).count())),
                new StatItem("Договоров с заказчиками (всего)", String.valueOf(customer.size())),
                new StatItem("Расторгнуто договоров с заказчиками", String.valueOf(terminated(customer))),
                new StatItem("Договоров с перевозчиками (всего)", String.valueOf(carrier.size())),
                new StatItem("Расторгнуто договоров с перевозчиками", String.valueOf(terminated(carrier))),
                new StatItem("Общий рейтинг", rating));
    }

    private static long terminated(List<ContractDto> contracts) {
        return contracts.stream().filter(c -> c.status() == ContractStatus.TERMINATED).count();
    }

    private TableDto statisticsTable() {
        List<List<Object>> rows = buildStatistics().stream()
                .map(item -> Arrays.<Object>asList(item.name(), item.value()))
                .collect(Collectors.toList());
        return new TableDto(STATISTICS_SHEET, List.of("Показатель", "Значение"), rows);
    }

    private TableDto usersTable() {
        List<List<Object>> rows = store.users.values().stream()
                .map(u -> row(u.id(), u.nickname(), u.email(), u.role().name(), u.active(), u.createdAt()))
                .toList();
        return new TableDto("users", List.of("id", "nickname", "email", "role", "is_active", "created_at"), rows);
    }

    private TableDto ordersTable() {
        List<List<Object>> rows = store.orders.values().stream()
                .map(o -> row(o.id(), o.customerId(), o.cargoDescription(), o.weightKg(), o.volumeM3(),
                        o.origin(), o.destination(), o.desiredDate(), o.status().name(), o.createdAt()))
                .toList();
        return new TableDto("orders", List.of("id", "customer_id", "cargo_description", "weight_kg", "volume_m3",
                "origin", "destination", "desired_date", "status", "created_at"), rows);
    }

    private TableDto cooperationTable() {
        List<List<Object>> rows = store.cooperationRequests.values().stream()
                .map(r -> row(r.id(), r.carrierId(), r.description(), r.vehicleInfo(), r.status().name(),
                        r.createdAt()))
                .toList();
        return new TableDto("cooperation_requests",
                List.of("id", "carrier_id", "description", "vehicle_info", "status", "created_at"), rows);
    }

    private TableDto contractsTable(ContractKind kind) {
        boolean customer = kind == ContractKind.CUSTOMER;
        List<List<Object>> rows = store.contracts.get(kind).values().stream()
                .map(c -> customer
                        ? row(c.id(), c.orderId(), c.counterpartyId(), c.forwarderId(), c.price(), c.terms(),
                                c.startDate(), c.endDate(), c.status().name(), c.forwarderId(), c.createdAt())
                        : row(c.id(), c.orderId(), store.carrierContractCooperation.get(c.id()),
                                c.counterpartyId(), c.forwarderId(), c.price(), c.terms(), c.startDate(),
                                c.endDate(), c.status().name(), c.forwarderId(), c.createdAt()))
                .toList();
        List<String> columns = customer
                ? List.of("id", "order_id", "customer_id", "forwarder_id", "price", "terms", "start_date",
                        "end_date", "status", "created_by", "created_at")
                : List.of("id", "order_id", "cooperation_id", "carrier_id", "forwarder_id", "price", "terms",
                        "start_date", "end_date", "status", "created_by", "created_at");
        return new TableDto(customer ? "customer_contracts" : "carrier_contracts", columns, rows);
    }

    private TableDto changeRequestsTable() {
        List<List<Object>> rows = store.changeRequests.values().stream()
                .map(r -> {
                    boolean customer = r.kind() == ContractKind.CUSTOMER;
                    return row(r.id(), customer ? r.contractId() : null, customer ? null : r.contractId(),
                            r.type().name(), r.initiatedBy(), r.proposedPrice(), r.proposedTerms(),
                            r.proposedStartDate(), r.proposedEndDate(), r.status().name(), r.createdAt(),
                            r.resolvedAt());
                })
                .toList();
        return new TableDto("contract_change_requests", List.of("id", "customer_contract_id",
                "carrier_contract_id", "type", "initiated_by", "proposed_price", "proposed_terms",
                "proposed_start_date", "proposed_end_date", "status", "created_at", "resolved_at"), rows);
    }

    private TableDto ratingsTable() {
        List<List<Object>> rows = store.ratings.values().stream()
                .map(r -> row(r.id(), r.orderId(), r.fromUserId(), r.toUserId(), r.score(), r.comment(),
                        r.createdAt()))
                .toList();
        return new TableDto("ratings",
                List.of("id", "order_id", "from_user_id", "to_user_id", "score", "comment", "created_at"), rows);
    }

    private TableDto notificationsTable() {
        List<List<Object>> rows = new ArrayList<>();
        store.notifications.forEach((userId, list) -> list.forEach(n ->
                rows.add(row(n.id(), userId, n.changeRequestId(), n.message(), n.read(), n.createdAt()))));
        rows.sort((a, b) -> Long.compare((Long) a.get(0), (Long) b.get(0)));
        return new TableDto("notifications",
                List.of("id", "user_id", "change_request_id", "message", "is_read", "created_at"), rows);
    }

    private static List<Object> row(Object... values) {
        List<Object> row = new ArrayList<>(values.length);
        for (Object value : values) {
            boolean text = value != null && !(value instanceof Number) && !(value instanceof Boolean)
                    && !(value instanceof String);
            row.add(text ? value.toString() : value);
        }
        return row;
    }

    private static void appendCsv(StringBuilder csv, TableDto table) {
        csv.append(table.name()).append("\r\n");
        csv.append(line(table.columns())).append("\r\n");
        table.rows().forEach(row -> csv.append(line(row)).append("\r\n"));
    }

    private static String line(List<?> values) {
        return values.stream().map(MockReportApi::escape).collect(Collectors.joining(";"));
    }

    private static String escape(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(";") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
