package com.beta.expedition.desktop.ui;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;

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

    public static void setPresent(TableColumn<?, ?> column, boolean present) {
        column.setVisible(present);
    }
}
