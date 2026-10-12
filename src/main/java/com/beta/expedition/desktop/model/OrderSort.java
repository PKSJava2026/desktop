package com.beta.expedition.desktop.model;

public enum OrderSort {
    DATE("По дате создания"),
    STATUS("По статусу"),
    WEIGHT("По весу");

    private final String label;

    OrderSort(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
