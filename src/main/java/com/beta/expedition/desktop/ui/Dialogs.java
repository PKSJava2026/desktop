package com.beta.expedition.desktop.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Dialogs {

    private static final String STYLESHEET = "/com/beta/expedition/desktop/css/style.css";

    private Dialogs() {
    }

    public static void showError(String message) {
        show(Alert.AlertType.ERROR, "Ошибка", message);
    }

    public static void showSuccess(String message) {
        show(Alert.AlertType.INFORMATION, "Готово", message);
    }

    public static boolean confirm(String message) {
        Alert alert = create(Alert.AlertType.CONFIRMATION, "Подтверждение", message);
        ButtonType yes = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);
        return alert.showAndWait().filter(result -> result == yes).isPresent();
    }

    public static <T> Optional<T> choose(String title, String message, List<T> options) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().setAll(
                new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        applyStyle(pane);
        return dialog.showAndWait();
    }

    public static void applyStyle(DialogPane pane) {
        pane.getStylesheets().add(Objects.requireNonNull(Dialogs.class.getResource(STYLESHEET)).toExternalForm());
        pane.getStyleClass().add("app-dialog");
    }

    private static void show(Alert.AlertType type, String title, String message) {
        create(type, title, message).showAndWait();
    }

    private static Alert create(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert.getDialogPane());
        return alert;
    }
}
