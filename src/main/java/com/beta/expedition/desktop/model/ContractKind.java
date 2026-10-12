package com.beta.expedition.desktop.model;

public enum ContractKind {
    CUSTOMER("customer", "Заказчик", "Договоры с заказчиками"),
    CARRIER("carrier", "Перевозчик", "Договоры с перевозчиками");

    private final String pathSegment;
    private final String counterpartyTitle;
    private final String title;

    ContractKind(String pathSegment, String counterpartyTitle, String title) {
        this.pathSegment = pathSegment;
        this.counterpartyTitle = counterpartyTitle;
        this.title = title;
    }

    public String pathSegment() {
        return pathSegment;
    }

    public String counterpartyTitle() {
        return counterpartyTitle;
    }

    @Override
    public String toString() {
        return title;
    }
}
