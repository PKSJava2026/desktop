package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.service.RatingService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.FormDialog;
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
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.util.Optional;

public class RateOrdersController {

    private static final String FORM_VIEW = "/com/beta/expedition/desktop/view/rating-form.fxml";

    @FXML
    private Label titleLabel;
    @FXML
    private Label hintLabel;
    @FXML
    private TableView<OrderDto> ordersTable;
    @FXML
    private TableColumn<OrderDto, String> numberColumn;
    @FXML
    private TableColumn<OrderDto, String> cargoColumn;
    @FXML
    private TableColumn<OrderDto, String> originColumn;
    @FXML
    private TableColumn<OrderDto, String> destinationColumn;
    @FXML
    private TableColumn<OrderDto, String> createdColumn;
    @FXML
    private Button rateButton;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator busyIndicator;

    private final RatingService service;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<OrderDto> orders = FXCollections.observableArrayList();

    public RateOrdersController(AppContext context, Navigator navigator) {
        this.service = context.ratingService();
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        titleLabel.setText("Оценка " + (service.ratesForwarder() ? "услуги экспедитора" : "работы перевозчика"));
        hintLabel.setText("Оценить можно только доставленную заявку, по одной оценке на заявку.");
        numberColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("№" + cell.getValue().id()));
        cargoColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.text(cell.getValue().cargoDescription())));
        originColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.text(cell.getValue().origin())));
        destinationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.text(cell.getValue().destination())));
        createdColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.dateTime(cell.getValue().createdAt())));
        ordersTable.setItems(orders);
        ordersTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onRate();
            }
        });

        FormBindings.showWhen(busyIndicator, busy);
        refreshButton.disableProperty().bind(busy);
        rateButton.disableProperty().bind(
                ordersTable.getSelectionModel().selectedItemProperty().isNull().or(busy));
        reload();
    }

    @FXML
    private void onRefresh() {
        reload();
    }

    @FXML
    private void onRate() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Navigator.LoadedView<RatingFormController> view = navigator.loadView(FORM_VIEW);
        RatingFormController form = view.controller();
        form.prepare(selected);
        Optional<RatingFormController.Rating> result = FormDialog.show("Оценка по заявке №" + selected.id(),
                "Оценить", view.root(), form::validate, form::toResult);
        result.ifPresent(rating -> Background.run(busy,
                () -> service.rate(selected.id(), rating.score(), rating.comment()),
                saved -> {
                    Dialogs.showSuccess("Оценка по заявке №" + selected.id() + " сохранена.");
                    reload();
                },
                Dialogs::showError));
    }

    private void reload() {
        Background.run(busy, service::unratedOrders, list -> orders.setAll(list), Dialogs::showError);
    }
}
