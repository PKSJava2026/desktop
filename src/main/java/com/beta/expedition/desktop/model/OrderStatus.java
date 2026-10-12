package com.beta.expedition.desktop.model;

public enum OrderStatus {
    NEW("Новая"),
    CONTRACTED("Договор заключён"),
    IN_TRANSIT("В пути"),
    DELIVERED("Доставлена"),
    CANCELLED("Отменена");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
