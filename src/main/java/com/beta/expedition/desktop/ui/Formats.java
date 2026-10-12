package com.beta.expedition.desktop.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formats {

    private static final String EMPTY = "—";
    private static final Locale RU = Locale.forLanguageTag("ru");
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

    public static String money(BigDecimal value) {
        return value == null ? EMPTY : String.format(RU, "%,.2f", value).replace('\u00A0', ' ');
    }

    public static String stars(int score, int max) {
        return "★".repeat(Math.max(0, score)) + "☆".repeat(Math.max(0, max - score));
    }

    public static String text(String value) {
        return value == null || value.isBlank() ? EMPTY : value;
    }
}
