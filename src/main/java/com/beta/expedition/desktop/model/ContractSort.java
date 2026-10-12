package com.beta.expedition.desktop.model;

public enum ContractSort {
    DATE("По дате создания"),
    STATUS("По статусу"),
    PRICE("По сумме");

    private final String label;

    ContractSort(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
