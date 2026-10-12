package com.beta.expedition.desktop.model;

public enum Role {
    CUSTOMER("Заказчик"),
    FORWARDER("Экспедитор"),
    CARRIER("Перевозчик"),
    ADMIN("Администратор");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
