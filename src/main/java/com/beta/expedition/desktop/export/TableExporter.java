package com.beta.expedition.desktop.export;

import com.beta.expedition.desktop.model.TableDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TableExporter {

    private TableExporter() {
    }

    public static Path export(TableDto table, ExportFormat format, Path target) {
        Path file = withExtension(target, format);
        byte[] content = format.render(table);
        try {
            Files.write(file, content);
        } catch (IOException e) {
            throw new ExportException("Не удалось сохранить файл «" + file.getFileName() + "»: проверьте, что "
                    + "папка доступна для записи и файл не открыт в другой программе.", e);
        }
        return file;
    }

    private static Path withExtension(Path target, ExportFormat format) {
        String suffix = "." + format.extension();
        String name = target.getFileName().toString();
        if (name.toLowerCase().endsWith(suffix)) {
            return target;
        }
        return target.resolveSibling(name + suffix);
    }
}
