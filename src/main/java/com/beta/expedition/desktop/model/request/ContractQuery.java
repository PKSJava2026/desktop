package com.beta.expedition.desktop.model.request;

import com.beta.expedition.desktop.model.ContractSort;
import com.beta.expedition.desktop.model.ContractStatus;
import java.time.LocalDate;

public record ContractQuery(
        ContractStatus status,
        LocalDate from,
        LocalDate to,
        LocalDate date,
        Long orderId,
        ContractSort sort,
        boolean descending) {

    public static ContractQuery all() {
        return new ContractQuery(null, null, null, null, null, null, false);
    }
}
