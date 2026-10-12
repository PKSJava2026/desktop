package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.OrderApi;
import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.StatItem;
import com.beta.expedition.desktop.model.request.OrderQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatisticsService {

    public static final int RECENT_DAYS = 30;

    private static final String EMPTY = "—";
    private static final Locale RU = Locale.forLanguageTag("ru");

    public record Indicator(String name, String display, Object value) {
    }

    public record Statistics(List<Indicator> orders, List<StatItem> company) {
    }

    private final OrderApi orderApi;
    private final ReportApi reportApi;

    public StatisticsService(OrderApi orderApi, ReportApi reportApi) {
        this.orderApi = orderApi;
        this.reportApi = reportApi;
    }

    public Statistics load(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new ValidationException("Период: дата «По» не может быть раньше даты «С».");
        }
        List<OrderDto> inPeriod = orderApi.search(new OrderQuery(null, null, from, to, null, false, null, false));
        List<OrderDto> recent = orderApi.search(new OrderQuery(null, null,
                LocalDate.now().minusDays(RECENT_DAYS), null, null, false, null, false));
        return new Statistics(ordersIndicators(inPeriod, recent.size()), reportApi.statistics());
    }

    private List<Indicator> ordersIndicators(List<OrderDto> orders, int recentCount) {
        List<Indicator> result = new ArrayList<>();
        result.add(count("Всего заявок", orders.size()));
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream().filter(order -> order.status() == status).count();
            result.add(count("Заявок в статусе «" + status + "»", count));
        }
        result.addAll(numeric(orders, "веса, кг", OrderDto::weightKg));
        result.addAll(numeric(orders, "объёма, м³", OrderDto::volumeM3));
        result.add(count("Создано за последние " + RECENT_DAYS + " дней", recentCount));
        return result;
    }

    private List<Indicator> numeric(List<OrderDto> orders, String caption, Function<OrderDto, BigDecimal> getter) {
        List<BigDecimal> values = orders.stream().map(getter).filter(value -> value != null)
                .collect(Collectors.toList());
        if (values.isEmpty()) {
            return List.of(
                    new Indicator("Минимум " + caption, EMPTY, null),
                    new Indicator("Максимум " + caption, EMPTY, null),
                    new Indicator("Среднее значение " + caption, EMPTY, null));
        }
        BigDecimal min = values.stream().min(Comparator.naturalOrder()).orElseThrow();
        BigDecimal max = values.stream().max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
        return List.of(
                new Indicator("Минимум " + caption, format(min), min),
                new Indicator("Максимум " + caption, format(max), max),
                new Indicator("Среднее значение " + caption, format(average), average));
    }

    private Indicator count(String name, long value) {
        return new Indicator(name, String.valueOf(value), value);
    }

    private String format(BigDecimal value) {
        return String.format(RU, "%,.2f", value).replace(' ', ' ');
    }
}
