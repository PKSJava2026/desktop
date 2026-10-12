package com.beta.expedition.desktop.api;

public enum ApiMode {
    MOCK("com.beta.expedition.desktop.api.mock.MockApi"),
    HTTP("com.beta.expedition.desktop.api.http.HttpApi");

    private final String implementationClass;

    ApiMode(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public String implementationClass() {
        return implementationClass;
    }
}
