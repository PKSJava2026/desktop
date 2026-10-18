package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.model.StatItem;
import com.beta.expedition.desktop.model.TableDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

final class HttpReportApi implements ReportApi {

    private static final TypeReference<List<StatItem>> STATS = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> NAMES = new TypeReference<>() {
    };

    private static final Map<String, String> LABELS = Map.of(
            "total_users", "Пользователей (всего)",
            "active_users", "Активных пользователей",
            "customers", "Заказчиков",
            "carriers", "Перевозчиков",
            "forwarders", "Экспедиторов",
            "total_orders", "Заявок на перевозку (всего)",
            "active_orders", "Заявок в работе",
            "delivered_orders", "Успешно выполнено заказов (доставлено)",
            "avg_rating", "Общий рейтинг");

    private final HttpTransport transport;

    HttpReportApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public List<StatItem> statistics() {
        return transport.get("/api/stats", STATS).stream()
                .map(item -> new StatItem(LABELS.getOrDefault(item.name(), item.name()), item.value()))
                .toList();
    }

    @Override
    public List<String> tables() {
        return transport.get("/api/tables", NAMES);
    }

    @Override
    public TableDto table(String name) {
        return transport.get("/api/tables/" + name, TableDto.class);
    }

    @Override
    public byte[] exportXlsx() {
        return transport.getBytes("/api/export/xlsx");
    }

    @Override
    public byte[] exportCsv() {
        return transport.getBytes("/api/export/csv");
    }
}
