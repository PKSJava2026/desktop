package com.beta.expedition.desktop.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public final class Navigator {

    private static final String VIEW_DIR = "/com/beta/expedition/desktop/view/";
    private static final String STYLESHEET = "/com/beta/expedition/desktop/css/style.css";
    private static final String LOGIN_VIEW = VIEW_DIR + "login.fxml";
    private static final String REGISTER_VIEW = VIEW_DIR + "register.fxml";
    private static final String MAIN_VIEW = VIEW_DIR + "main.fxml";

    private final AppContext context;
    private final Scene scene;

    public Navigator(Stage stage, AppContext context) {
        this.context = context;
        this.scene = new Scene(new StackPane(), 960, 640);
        scene.getStylesheets().add(
                Objects.requireNonNull(Navigator.class.getResource(STYLESHEET)).toExternalForm());
        stage.setTitle("Экспедиторская компания");
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.setScene(scene);
    }

    public void showLogin() {
        scene.setRoot(load(LOGIN_VIEW));
    }

    public void showRegister() {
        scene.setRoot(load(REGISTER_VIEW));
    }

    public void showMain() {
        scene.setRoot(load(MAIN_VIEW));
    }

    public Parent load(String viewPath) {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                Navigator.class.getResource(viewPath), "Не найден экран " + viewPath));
        loader.setControllerFactory(this::createController);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить экран " + viewPath, e);
        }
    }

    private Object createController(Class<?> type) {
        try {
            return type.getConstructor(AppContext.class, Navigator.class).newInstance(context, this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать контроллер " + type.getName(), e);
        }
    }
}
