package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class ProfileController {

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
        createdLabel.setText(Formats.dateTime(user.createdAt()));
    }
}
