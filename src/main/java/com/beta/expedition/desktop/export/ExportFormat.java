package com.beta.expedition.desktop.export;

import com.beta.expedition.desktop.model.TableDto;

import java.util.List;
import java.util.function.Function;

public enum ExportFormat {
    CSV("csv", "Файл CSV (*.csv)", CsvWriter::write),
    XLSX("xlsx", "Книга Excel (*.xlsx)", table -> XlsxWriter.write(List.of(table)));

    private final String extension;
    private final String description;
    private final Function<TableDto, byte[]> writer;

    ExportFormat(String extension, String description, Function<TableDto, byte[]> writer) {
        this.extension = extension;
        this.description = description;
        this.writer = writer;
    }

    public String extension() {
        return extension;
    }

    public String description() {
        return description;
    }

    public byte[] render(TableDto table) {
        return writer.apply(table);
    }
}
