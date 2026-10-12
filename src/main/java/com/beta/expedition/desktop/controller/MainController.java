package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Section;
import com.beta.expedition.desktop.ui.Sections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private Label userLabel;
    @FXML
    private VBox navBox;
    @FXML
    private StackPane contentPane;

    private final AppContext context;
    private final Navigator navigator;

    public MainController(AppContext context, Navigator navigator) {
        this.context = context;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        UserDto user = context.session().user();
        userLabel.setText(user.nickname() + " · " + user.role());

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null && previous != null) {
                previous.setSelected(true);
            }
        });

        ToggleButton first = null;
        for (Section section : Sections.forRole(user.role())) {
            ToggleButton button = new ToggleButton(section.title());
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setToggleGroup(group);
            button.setOnAction(event -> showSection(section));
            navBox.getChildren().add(button);
            if (first == null) {
                first = button;
            }
        }
        if (first != null) {
            first.setSelected(true);
            first.fire();
        }
    }

    @FXML
    private void onLogout() {
        context.authService().logout();
        navigator.showLogin();
    }

    private void showSection(Section section) {
        contentPane.getChildren().setAll(navigator.load(section.viewPath()));
    }
}
