package com.beta.expedition.desktop.model;

import java.util.List;

public record TableDto(
        String name,
        List<String> columns,
        List<List<Object>> rows) {
}
