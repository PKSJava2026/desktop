package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField nicknameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmField;
    @FXML
    private ComboBox<Role> roleBox;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private Button registerButton;

    private final AppContext context;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();

    public RegisterController(AppContext context, Navigator navigator) {
        this.context = context;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(Role.CUSTOMER, Role.CARRIER));
        roleBox.getSelectionModel().selectFirst();
        FormBindings.showWhenNotEmpty(errorLabel);
        FormBindings.showWhen(busyIndicator, busy);
        registerButton.disableProperty().bind(busy);
    }

    @FXML
    private void onRegister() {
        errorLabel.setText("");
        String nickname = nicknameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmation = confirmField.getText();
        Role role = roleBox.getValue();
        Background.run(busy,
                () -> context.authService().register(nickname, email, password, confirmation, role),
                user -> navigator.showMain(),
                errorLabel::setText);
    }

    @FXML
    private void openLogin() {
        navigator.showLogin();
    }
}
