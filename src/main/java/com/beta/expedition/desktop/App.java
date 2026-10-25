package com.beta.expedition.desktop;

import com.beta.expedition.desktop.api.Api;
import com.beta.expedition.desktop.api.ApiException;
import com.beta.expedition.desktop.api.http.HttpApi;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static final String ICON = "/com/beta/expedition/desktop/images/app-icon.png";

    @Override
    public void start(Stage stage) {
        try {
            Api api = new HttpApi();
            stage.getIcons().add(new Image(App.class.getResourceAsStream(ICON)));
            Navigator navigator = new Navigator(stage, new AppContext(api));
            navigator.showLogin();
            stage.show();
        } catch (ApiException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.setTitle("Экспедиторская компания");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
