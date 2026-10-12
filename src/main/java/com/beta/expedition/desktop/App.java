package com.beta.expedition.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private static final String MAIN_VIEW = "/com/beta/expedition/desktop/view/main.fxml";
    private static final String STYLESHEET = "/com/beta/expedition/desktop/css/style.css";

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource(MAIN_VIEW)));
        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(Objects.requireNonNull(App.class.getResource(STYLESHEET)).toExternalForm());

        stage.setTitle("Экспедиторская компания");
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
