package com.beta.expedition.desktop.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Formats {

    private static final String EMPTY = "—";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Formats() {
    }

    public static String date(LocalDate value) {
        return value == null ? EMPTY : value.format(DATE);
    }

    public static String dateTime(OffsetDateTime value) {
        return value == null ? EMPTY : value.atZoneSameInstant(ZoneId.systemDefault()).format(DATE_TIME);
    }

    public static String decimal(BigDecimal value) {
        return value == null ? EMPTY : value.stripTrailingZeros().toPlainString();
    }

    public static String text(String value) {
        return value == null || value.isBlank() ? EMPTY : value;
    }
}
