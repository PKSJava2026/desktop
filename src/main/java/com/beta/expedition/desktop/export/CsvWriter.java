package com.beta.expedition.desktop.export;

import com.beta.expedition.desktop.model.TableDto;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvWriter {

    private static final String BOM = "﻿";
    private static final String SEPARATOR = ";";
    private static final String LINE_END = "\r\n";

    private CsvWriter() {
    }

    public static byte[] write(TableDto table) {
        StringBuilder csv = new StringBuilder(BOM);
        appendRows(csv, table);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void appendRows(StringBuilder csv, TableDto table) {
        csv.append(line(table.columns())).append(LINE_END);
        table.rows().forEach(row -> csv.append(line(row)).append(LINE_END));
    }

    private static String line(List<?> values) {
        return values.stream().map(CsvWriter::escape).collect(Collectors.joining(SEPARATOR));
    }

    private static String escape(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(SEPARATOR) || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
