package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField nicknameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private Button loginButton;

    private final AppContext context;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();

    public LoginController(AppContext context, Navigator navigator) {
        this.context = context;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
        FormBindings.showWhen(busyIndicator, busy);
        loginButton.disableProperty().bind(busy);
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String nickname = nicknameField.getText();
        String password = passwordField.getText();
        Background.run(busy,
                () -> context.authService().login(nickname, password),
                user -> navigator.showMain(),
                errorLabel::setText);
    }

    @FXML
    private void openRegister() {
        navigator.showRegister();
    }
}
