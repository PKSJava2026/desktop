package com.beta.expedition.desktop.model;

public enum ChangeRequestStatus {
    PENDING("Ожидает ответа"),
    ACCEPTED("Принят"),
    REJECTED("Отклонён"),
    CANCELLED("Отозван");

    private final String label;

    ChangeRequestStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
