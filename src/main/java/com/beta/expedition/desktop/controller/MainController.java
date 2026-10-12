package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.service.NotificationService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Section;
import com.beta.expedition.desktop.ui.Sections;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static final System.Logger LOGGER = System.getLogger(MainController.class.getName());

    private final AppContext context;
    private final Navigator navigator;
    private final BooleanProperty counterBusy = new SimpleBooleanProperty();

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
            if (Sections.NOTIFICATIONS_VIEW.equals(section.viewPath())) {
                bindUnreadCounter(button, section.title());
            }
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
        context.notificationService().unreadProperty().set(0);
        navigator.showLogin();
    }

    private void showSection(Section section) {
        refreshUnreadCounter();
        contentPane.getChildren().setAll(navigator.load(section.viewPath()));
    }

    private void bindUnreadCounter(ToggleButton button, String title) {
        IntegerProperty unread = context.notificationService().unreadProperty();
        button.textProperty().bind(Bindings.createStringBinding(
                () -> unread.get() > 0 ? title + " (" + unread.get() + ")" : title, unread));
    }

    private void refreshUnreadCounter() {
        NotificationService service = context.notificationService();
        Background.run(counterBusy, service::unreadCount,
                count -> service.unreadProperty().set(count),
                message -> LOGGER.log(System.Logger.Level.WARNING, "Не удалось обновить счётчик уведомлений: {0}",
                        message));
    }
}
