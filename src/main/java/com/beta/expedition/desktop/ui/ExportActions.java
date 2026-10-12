package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.export.ExportFormat;
import com.beta.expedition.desktop.export.TableExporter;
import com.beta.expedition.desktop.model.TableDto;
import javafx.beans.property.BooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class ExportActions {

    private ExportActions() {
    }

    public static void export(Window owner, ExportFormat format, String defaultName,
                              Supplier<TableDto> table, BooleanProperty busy) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить как " + format.extension().toUpperCase());
        chooser.setInitialFileName(defaultName + "." + format.extension());
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.description(), "*." + format.extension()));
        File chosen = chooser.showSaveDialog(owner);
        if (chosen == null) {
            return;
        }
        TableDto data = table.get();
        Background.run(busy,
                () -> TableExporter.export(data, format, chosen.toPath()),
                (Path saved) -> Dialogs.showSuccess("Файл сохранён:\n" + saved.toAbsolutePath()),
                Dialogs::showError);
    }
}
