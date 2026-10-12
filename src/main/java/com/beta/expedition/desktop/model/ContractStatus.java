package com.beta.expedition.desktop.model;

public enum ContractStatus {
    PENDING("Ожидает подтверждения"),
    ACTIVE("Действует"),
    TERMINATED("Расторгнут"),
    COMPLETED("Завершён"),
    REJECTED("Отклонён");

    private final String label;

    ContractStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
