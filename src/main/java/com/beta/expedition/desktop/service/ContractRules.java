package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.ui.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ContractRules {

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private static final int TERMS_MAX = 1000;

    private ContractRules() {
    }

    public static Validator price(Validator validator, String price, boolean required) {
        if (required) {
            validator.required("Цена", price);
        }
        return validator
                .number("Цена", price)
                .positive("Цена", price)
                .range("Цена", price, MIN_PRICE, MAX_PRICE)
                .scale("Цена", price, 2);
    }

    public static Validator terms(Validator validator, String terms) {
        return validator.length("Условия", terms, 0, TERMS_MAX);
    }

    public static Validator dates(Validator validator, LocalDate start, LocalDate end) {
        return validator
                .notInPast("Дата начала", start)
                .notInPast("Дата окончания", end)
                .notBefore("Дата окончания", end, "Дата начала", start);
    }
}
