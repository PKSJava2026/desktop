package com.beta.expedition.desktop.model;

public enum CooperationStatus {
    NEW("Новая"),
    PROCESSED("Обработана"),
    CANCELLED("Отозвана");

    private final String label;

    CooperationStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
