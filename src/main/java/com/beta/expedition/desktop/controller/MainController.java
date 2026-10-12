package com.beta.expedition.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("Приложение запущено. Вход и разделы появятся в следующих задачах.");
    }
}
