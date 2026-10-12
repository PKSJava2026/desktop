package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.NotificationDto;
import com.beta.expedition.desktop.service.NotificationService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.List;

public class NotificationsController {

    private static final String UNREAD_STYLE = "unread-row";

    @FXML
    private TableView<NotificationDto> notificationsTable;
    @FXML
    private TableColumn<NotificationDto, String> dateColumn;
    @FXML
    private TableColumn<NotificationDto, String> messageColumn;
    @FXML
    private TableColumn<NotificationDto, String> stateColumn;
    @FXML
    private Button markReadButton;
    @FXML
    private Button markAllButton;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private Label counterLabel;
    @FXML
    private Label detailsHint;
    @FXML
    private GridPane detailsGrid;
    @FXML
    private Label detailsDate;
    @FXML
    private Label detailsState;
    @FXML
    private Label detailsMessage;

    private final NotificationService service;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<NotificationDto> notifications = FXCollections.observableArrayList();

    public NotificationsController(AppContext context, Navigator navigator) {
        this.service = context.notificationService();
    }

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.dateTime(cell.getValue().createdAt())));
        messageColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().message()));
        stateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().read() ? "Прочитано" : "Новое"));
        dateColumn.setSortable(false);
        messageColumn.setSortable(false);
        stateColumn.setSortable(false);
        notificationsTable.setItems(notifications);
        notificationsTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(NotificationDto item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove(UNREAD_STYLE);
                if (!empty && item != null && !item.read()) {
                    getStyleClass().add(UNREAD_STYLE);
                }
            }
        });
        notificationsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> showSelection(current));
        notificationsTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onMarkRead();
            }
        });

        FormBindings.showWhen(busyIndicator, busy);
        FormBindings.showWhen(detailsHint, notificationsTable.getSelectionModel().selectedItemProperty().isNull());
        FormBindings.showWhen(detailsGrid, notificationsTable.getSelectionModel().selectedItemProperty().isNotNull());
        refreshButton.disableProperty().bind(busy);
        markAllButton.disableProperty().bind(busy.or(service.unreadProperty().isEqualTo(0)));
        counterLabel.textProperty().bind(service.unreadProperty().asString("Непрочитанных: %d"));
        showSelection(null);
        reload(null);
    }

    @FXML
    private void onRefresh() {
        NotificationDto selected = notificationsTable.getSelectionModel().getSelectedItem();
        reload(selected == null ? null : selected.id());
    }

    @FXML
    private void onMarkRead() {
        NotificationDto selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.read()) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.markRead(selected.id());
                    return selected.id();
                },
                this::reload,
                Dialogs::showError);
    }

    @FXML
    private void onMarkAll() {
        Background.run(busy,
                service::markAllRead,
                count -> reload(null),
                Dialogs::showError);
    }

    private void reload(Long selectId) {
        Background.run(busy,
                service::list,
                list -> show(list, selectId),
                Dialogs::showError);
    }

    private void show(List<NotificationDto> list, Long selectId) {
        notifications.setAll(list);
        service.unreadProperty().set(NotificationService.countUnread(list));
        if (selectId != null) {
            notifications.stream().filter(item -> item.id().equals(selectId)).findFirst()
                    .ifPresent(item -> notificationsTable.getSelectionModel().select(item));
        }
        showSelection(notificationsTable.getSelectionModel().getSelectedItem());
    }

    private void showSelection(NotificationDto notification) {
        markReadButton.setDisable(notification == null || notification.read());
        if (notification == null) {
            return;
        }
        detailsDate.setText(Formats.dateTime(notification.createdAt()));
        detailsState.setText(notification.read() ? "Прочитано" : "Новое");
        detailsMessage.setText(notification.message());
    }
}
