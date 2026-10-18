package com.beta.expedition.desktop.api.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class Query {

    private final StringBuilder text = new StringBuilder();

    Query add(String name, Object value) {
        String string = value instanceof Enum<?> e ? e.name() : value == null ? null : value.toString();
        if (string != null && !string.isBlank()) {
            text.append(text.length() == 0 ? '?' : '&')
                    .append(name).append('=')
                    .append(URLEncoder.encode(string, StandardCharsets.UTF_8));
        }
        return this;
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
