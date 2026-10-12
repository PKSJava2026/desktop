package com.beta.expedition.desktop.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Validator {

    private final List<String> errors = new ArrayList<>();

    public static Optional<BigDecimal> parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(value.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Validator required(String field, String value) {
        if (value == null || value.isBlank()) {
            errors.add("Поле «" + field + "»: заполните значение.");
        }
        return this;
    }

    public Validator requiredValue(String field, Object value) {
        if (value == null) {
            errors.add("Поле «" + field + "»: выберите значение.");
        }
        return this;
    }

    public Validator length(String field, String value, int min, int max) {
        if (value == null || value.isBlank()) {
            return this;
        }
        int length = value.trim().length();
        if (length < min) {
            errors.add("Поле «" + field + "»: введите не менее " + min + " символов.");
        } else if (length > max) {
            errors.add("Поле «" + field + "»: сократите текст до " + max + " символов.");
        }
        return this;
    }

    public Validator matches(String field, String value, Pattern pattern, String hint) {
        if (value != null && !value.isBlank() && !pattern.matcher(value.trim()).matches()) {
            errors.add("Поле «" + field + "»: " + hint);
        }
        return this;
    }

    public Validator number(String field, String value) {
        if (value != null && !value.isBlank() && parseDecimal(value).isEmpty()) {
            errors.add("Поле «" + field + "»: введите число, например 12.5.");
        }
        return this;
    }

    public Validator positive(String field, String value) {
        parseDecimal(value).ifPresent(number -> {
            if (number.signum() <= 0) {
                errors.add("Поле «" + field + "»: введите число больше 0.");
            }
        });
        return this;
    }

    public Validator range(String field, String value, BigDecimal min, BigDecimal max) {
        parseDecimal(value).ifPresent(number -> {
            if (number.compareTo(min) < 0 || number.compareTo(max) > 0) {
                errors.add("Поле «" + field + "»: введите число от " + min.toPlainString()
                        + " до " + max.toPlainString() + ".");
            }
        });
        return this;
    }

    public Validator scale(String field, String value, int maxScale) {
        parseDecimal(value).ifPresent(number -> {
            if (number.stripTrailingZeros().scale() > maxScale) {
                errors.add("Поле «" + field + "»: укажите не более " + maxScale + " знаков после запятой.");
            }
        });
        return this;
    }

    public Validator notInPast(String field, LocalDate value) {
        if (value != null && value.isBefore(LocalDate.now())) {
            errors.add("Поле «" + field + "»: дата не может быть в прошлом.");
        }
        return this;
    }

    public Validator notBefore(String field, LocalDate value, String otherField, LocalDate other) {
        if (value != null && other != null && value.isBefore(other)) {
            errors.add("Поле «" + field + "»: дата не может быть раньше поля «" + otherField + "».");
        }
        return this;
    }

    public Validator check(boolean valid, String message) {
        if (!valid) {
            errors.add(message);
        }
        return this;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }

    public String message() {
        return String.join("\n", errors);
    }

    public boolean validateOrShow() {
        if (isValid()) {
            return true;
        }
        Dialogs.showError(message());
        return false;
    }
}
