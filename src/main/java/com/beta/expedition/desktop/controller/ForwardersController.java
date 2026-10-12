package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.ForwarderRequest;
import com.beta.expedition.desktop.service.ForwarderService;
import com.beta.expedition.desktop.service.ValidationException;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class ForwardersController {

    private static final String FORM_VIEW = "/com/beta/expedition/desktop/view/forwarder-form.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private TableView<UserDto> forwardersTable;
    @FXML
    private TableColumn<UserDto, String> nicknameColumn;
    @FXML
    private TableColumn<UserDto, String> emailColumn;
    @FXML
    private TableColumn<UserDto, String> createdColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator busyIndicator;

    private final ForwarderService service;
    private final ObservableList<UserDto> forwarders = FXCollections.observableArrayList();
    private final BooleanProperty busy = new SimpleBooleanProperty();

    public ForwardersController(AppContext context, Navigator navigator) {
        this.service = new ForwarderService(context.api().users());
    }

    @FXML
    private void initialize() {
        nicknameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().nickname()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().email()));
        createdColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDate(cell.getValue())));
        forwardersTable.setItems(forwarders);
        forwardersTable.setPlaceholder(new Label("Экспедиторов пока нет"));

        FormBindings.showWhen(busyIndicator, busy);
        addButton.disableProperty().bind(busy);
        refreshButton.disableProperty().bind(busy);
        removeButton.disableProperty().bind(
                forwardersTable.getSelectionModel().selectedItemProperty().isNull().or(busy));

        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAdd() {
        askForwarder().ifPresent(request -> Background.run(busy,
                () -> service.create(request),
                created -> {
                    Dialogs.showSuccess("Экспедитор «" + created.nickname() + "» назначен.");
                    refresh();
                },
                Dialogs::showError));
    }

    @FXML
    private void onRemove() {
        UserDto selected = forwardersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean confirmed = Dialogs.confirm("Удалить экспедитора «" + selected.nickname()
                + "»? Он больше не сможет войти в систему.");
        if (!confirmed) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.remove(selected.id());
                    return selected;
                },
                removed -> {
                    Dialogs.showSuccess("Экспедитор «" + removed.nickname() + "» удалён.");
                    refresh();
                },
                Dialogs::showError);
    }

    private void refresh() {
        Background.run(busy, service::list, list -> forwarders.setAll(list), Dialogs::showError);
    }

    private Optional<ForwarderRequest> askForwarder() {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                ForwardersController.class.getResource(FORM_VIEW), "Не найден экран " + FORM_VIEW));
        Parent form;
        try {
            form = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить экран " + FORM_VIEW, e);
        }
        ForwarderFormController formController = loader.getController();

        ButtonType save = new ButtonType("Назначить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ForwarderRequest> dialog = new Dialog<>();
        dialog.setTitle("Новый экспедитор");
        dialog.initOwner(forwardersTable.getScene().getWindow());
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(forwardersTable.getScene().getStylesheets());
        pane.getStyleClass().add("app-dialog");
        pane.setContent(form);
        pane.getButtonTypes().addAll(save, cancel);

        Button saveButton = (Button) pane.lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                service.validate(formController.toRequest(), formController.confirmation());
                formController.showError("");
            } catch (ValidationException e) {
                formController.showError(e.getMessage());
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save ? formController.toRequest() : null);
        Platform.runLater(formController::focusFirstField);
        return dialog.showAndWait();
    }

    private static String formatDate(UserDto user) {
        return user.createdAt() == null ? "—"
                : user.createdAt().atZoneSameInstant(ZoneId.systemDefault()).format(DATE_FORMAT);
    }
}
