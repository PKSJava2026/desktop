package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private Label nicknameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label createdLabel;

    private final AppContext context;

    public ProfileController(AppContext context, Navigator navigator) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        UserDto user = context.session().user();
        nicknameLabel.setText(user.nickname());
        emailLabel.setText(user.email());
        roleLabel.setText(user.role().toString());
        createdLabel.setText(user.createdAt() == null ? "—"
                : user.createdAt().atZoneSameInstant(ZoneId.systemDefault()).format(DATE_FORMAT));
    }
}
