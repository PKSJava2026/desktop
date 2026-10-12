package com.beta.expedition.desktop.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

public final class Tables {

    private Tables() {
    }

    public static <S, T> void formatted(TableColumn<S, T> column, Function<T, String> formatter) {
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatter.apply(item));
            }
        });
    }
}
