package com.beta.expedition.desktop.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class FormDialog {

    private FormDialog() {
    }

    public static <T> Optional<T> show(String title, String confirmText, Parent content,
                                       BooleanSupplier isValid, Supplier<T> result) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(content);
        Dialogs.applyStyle(pane);

        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(confirm, cancel);

        Node confirmButton = pane.lookupButton(confirm);
        confirmButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!isValid.getAsBoolean()) {
                event.consume();
                Platform.runLater(() -> pane.getScene().getWindow().sizeToScene());
            }
        });
        dialog.setResultConverter(button -> button == confirm ? result.get() : null);
        return dialog.showAndWait();
    }
}
