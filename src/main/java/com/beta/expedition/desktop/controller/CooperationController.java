package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.service.CooperationService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.FormDialog;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Tables;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

import java.time.OffsetDateTime;
import java.util.List;

public class CooperationController {

    private static final String REQUEST_FORM_VIEW = "/com/beta/expedition/desktop/view/cooperation-form.fxml";
    private static final String CONTRACT_FORM_VIEW =
            "/com/beta/expedition/desktop/view/cooperation-contract-form.fxml";

    @FXML
    private Label titleLabel;
    @FXML
    private Button createButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button contractButton;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private Label countLabel;
    @FXML
    private TableView<CooperationRequestDto> requestsTable;
    @FXML
    private TableColumn<CooperationRequestDto, Long> idColumn;
    @FXML
    private TableColumn<CooperationRequestDto, String> carrierColumn;
    @FXML
    private TableColumn<CooperationRequestDto, String> vehicleColumn;
    @FXML
    private TableColumn<CooperationRequestDto, String> descriptionColumn;
    @FXML
    private TableColumn<CooperationRequestDto, CooperationStatus> statusColumn;
    @FXML
    private TableColumn<CooperationRequestDto, OffsetDateTime> createdColumn;
    @FXML
    private Label detailsHint;
    @FXML
    private GridPane detailsGrid;
    @FXML
    private Label detailsNumber;
    @FXML
    private Label detailsStatus;
    @FXML
    private Label detailsCarrierCaption;
    @FXML
    private Label detailsCarrier;
    @FXML
    private Label detailsVehicle;
    @FXML
    private Label detailsDescription;
    @FXML
    private Label detailsCreated;

    private final CooperationService service;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<CooperationRequestDto> requests = FXCollections.observableArrayList();
    private final SortedList<CooperationRequestDto> sorted = new SortedList<>(requests);

    public CooperationController(AppContext context, Navigator navigator) {
        this.service = context.cooperationService();
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        boolean carrier = service.isCarrier();
        titleLabel.setText(carrier ? "Мои заявки о сотрудничестве" : "Заявки перевозчиков о сотрудничестве");
        FormBindings.setPresent(createButton, carrier);
        FormBindings.setPresent(cancelButton, carrier);
        FormBindings.setPresent(contractButton, !carrier);
        FormBindings.setPresent(detailsCarrierCaption, !carrier);
        FormBindings.setPresent(detailsCarrier, !carrier);
        carrierColumn.setVisible(!carrier);

        FormBindings.showWhen(busyIndicator, busy);
        configureColumns();
        sorted.comparatorProperty().bind(requestsTable.comparatorProperty());
        requestsTable.setItems(sorted);
        requestsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> showSelection(current));
        FormBindings.showWhen(detailsHint, requestsTable.getSelectionModel().selectedItemProperty().isNull());
        FormBindings.showWhen(detailsGrid, requestsTable.getSelectionModel().selectedItemProperty().isNotNull());
        showSelection(null);
        reload(null);
    }

    @FXML
    private void onRefresh() {
        CooperationRequestDto selected = requestsTable.getSelectionModel().getSelectedItem();
        reload(selected == null ? null : selected.id());
    }

    @FXML
    private void onCreate() {
        Navigator.LoadedView<CooperationFormController> view = navigator.loadView(REQUEST_FORM_VIEW);
        CooperationFormController form = view.controller();
        FormDialog.show("Новая заявка о сотрудничестве", "Отправить", view.root(), form::validate, form::toForm)
                .ifPresent(request -> Background.run(busy,
                        () -> service.create(request),
                        created -> reload(created.id()),
                        Dialogs::showError));
    }

    @FXML
    private void onCancelRequest() {
        CooperationRequestDto selected = requestsTable.getSelectionModel().getSelectedItem();
        if (!service.canCancel(selected)
                || !Dialogs.confirm("Отозвать заявку о сотрудничестве №" + selected.id() + "?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.cancel(selected.id());
                    return selected.id();
                },
                this::reload,
                Dialogs::showError);
    }

    @FXML
    private void onCreateContract() {
        CooperationRequestDto selected = requestsTable.getSelectionModel().getSelectedItem();
        if (!service.canCreateContract(selected)) {
            return;
        }
        Background.run(busy, service::ordersForContract,
                orders -> openContractForm(selected, orders),
                Dialogs::showError);
    }

    private void openContractForm(CooperationRequestDto request, List<OrderDto> orders) {
        if (orders.isEmpty()) {
            Dialogs.showError("Нет заявок на перевозку, по которым можно заключить договор с перевозчиком. "
                    + "Нужна заявка в статусе «Договор заключён».");
            return;
        }
        Navigator.LoadedView<CooperationContractFormController> view = navigator.loadView(CONTRACT_FORM_VIEW);
        CooperationContractFormController form = view.controller();
        form.prepare(request, orders);
        FormDialog.show("Договор с перевозчиком по заявке №" + request.id(), "Предложить договор",
                        view.root(), form::validate, form::toRequest)
                .ifPresent(contractRequest -> Background.run(busy,
                        () -> service.createContract(request.id(), contractRequest),
                        contract -> {
                            reload(null);
                            Dialogs.showSuccess("Договор №" + contract.id() + " предложен перевозчику "
                                    + request.carrierNickname() + " и ждёт его подтверждения.");
                        },
                        Dialogs::showError));
    }

    private void reload(Long selectId) {
        Background.run(busy,
                service::list,
                list -> {
                    requests.setAll(list);
                    countLabel.setText("Заявок: " + list.size());
                    if (selectId != null) {
                        requests.stream().filter(request -> request.id().equals(selectId)).findFirst()
                                .ifPresent(request -> requestsTable.getSelectionModel().select(request));
                    }
                    showSelection(requestsTable.getSelectionModel().getSelectedItem());
                },
                Dialogs::showError);
    }

    private void showSelection(CooperationRequestDto request) {
        cancelButton.setDisable(!service.canCancel(request));
        contractButton.setDisable(!service.canCreateContract(request));
        if (request == null) {
            return;
        }
        detailsNumber.setText("№" + request.id());
        detailsStatus.setText(request.status().toString());
        detailsCarrier.setText(Formats.text(request.carrierNickname()));
        detailsVehicle.setText(Formats.text(request.vehicleInfo()));
        detailsDescription.setText(Formats.text(request.description()));
        detailsCreated.setText(Formats.dateTime(request.createdAt()));
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().id()));
        carrierColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().carrierNickname()));
        vehicleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().vehicleInfo()));
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().description()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().status()));
        createdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().createdAt()));
        Tables.formatted(createdColumn, Formats::dateTime);
        idColumn.setSortType(TableColumn.SortType.DESCENDING);
        requestsTable.getSortOrder().add(idColumn);
    }
}
