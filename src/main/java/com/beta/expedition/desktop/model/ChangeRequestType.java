package com.beta.expedition.desktop.model;

public enum ChangeRequestType {
    AMEND("Изменение"),
    TERMINATE("Расторжение");

    private final String label;

    ChangeRequestType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
