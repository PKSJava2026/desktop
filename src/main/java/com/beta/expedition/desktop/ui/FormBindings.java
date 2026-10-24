package com.beta.expedition.desktop.ui;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.function.Predicate;

public final class FormBindings {

    private FormBindings() {
    }

    public static void showWhen(Node node, ObservableBooleanValue condition) {
        node.visibleProperty().bind(condition);
        node.managedProperty().bind(condition);
    }

    public static void showWhenNotEmpty(Label label) {
        showWhen(label, label.textProperty().isNotEmpty());
    }

    public static void setPresent(Node node, boolean present) {
        node.setVisible(present);
        node.setManaged(present);
    }

    public static <T> void dimRowsWhen(TableView<T> table, Predicate<T> archived) {
        table.setRowFactory(view -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("archived-row");
                if (!empty && item != null && archived.test(item)) {
                    getStyleClass().add("archived-row");
                }
            }
        });
    }

    public static void setPresent(TableColumn<?, ?> column, boolean present) {
        column.setVisible(present);
    }
}
