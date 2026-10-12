package com.beta.expedition.desktop.model.request;

import com.beta.expedition.desktop.model.OrderSort;
import com.beta.expedition.desktop.model.OrderStatus;
import java.time.LocalDate;

public record OrderQuery(
        String text,
        OrderStatus status,
        LocalDate from,
        LocalDate to,
        Long customerId,
        boolean activeOnly,
        OrderSort sort,
        boolean descending) {

    public static OrderQuery all() {
        return new OrderQuery(null, null, null, null, null, false, null, false);
    }
}
