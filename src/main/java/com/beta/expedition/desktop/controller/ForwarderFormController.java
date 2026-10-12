package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.request.ForwarderRequest;
import com.beta.expedition.desktop.ui.FormBindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForwarderFormController {

    @FXML
    private TextField nicknameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmationField;
    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
    }

    public ForwarderRequest toRequest() {
        return new ForwarderRequest(nicknameField.getText(), emailField.getText(), passwordField.getText());
    }

    public String confirmation() {
        return confirmationField.getText();
    }

    public void showError(String message) {
        errorLabel.setText(message);
        if (errorLabel.getScene() != null) {
            errorLabel.getScene().getWindow().sizeToScene();
        }
    }

    public void focusFirstField() {
        nicknameField.requestFocus();
    }
}
