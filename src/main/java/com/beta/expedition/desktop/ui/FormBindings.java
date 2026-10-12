package com.beta.expedition.desktop.ui;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;

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
}
