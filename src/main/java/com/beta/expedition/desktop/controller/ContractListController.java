package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ChangeRequestType;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.service.ContractService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.FormDialog;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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

import java.util.ArrayList;
import java.util.List;

public class ContractListController {

    private static final String FORM_VIEW = "/com/beta/expedition/desktop/view/contract-form.fxml";

    private record Snapshot(List<ContractDto> contracts, List<ChangeRequestDto> requests) {
    }

    private record OfferData(List<UserDto> counterparties, List<OrderDto> orders) {
    }

    @FXML
    private Button offerButton;
    @FXML
    private Button acceptButton;
    @FXML
    private Button rejectButton;
    @FXML
    private Button amendButton;
    @FXML
    private Button terminateButton;
    @FXML
    private Button respondAcceptButton;
    @FXML
    private Button respondRejectButton;
    @FXML
    private Button cancelRequestButton;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private Label countLabel;
    @FXML
    private TableView<ContractDto> contractsTable;
    @FXML
    private TableColumn<ContractDto, Long> idColumn;
    @FXML
    private TableColumn<ContractDto, String> orderColumn;
    @FXML
    private TableColumn<ContractDto, String> counterpartyColumn;
    @FXML
    private TableColumn<ContractDto, String> forwarderColumn;
    @FXML
    private TableColumn<ContractDto, String> priceColumn;
    @FXML
    private TableColumn<ContractDto, String> statusColumn;
    @FXML
    private Label detailsHint;
    @FXML
    private GridPane detailsGrid;
    @FXML
    private Label detailsNumber;
    @FXML
    private Label detailsStatus;
    @FXML
    private Label detailsOrder;
    @FXML
    private Label detailsCounterpartyCaption;
    @FXML
    private Label detailsCounterparty;
    @FXML
    private Label detailsForwarder;
    @FXML
    private Label detailsPrice;
    @FXML
    private Label detailsTerms;
    @FXML
    private Label detailsPeriod;
    @FXML
    private Label detailsCreated;
    @FXML
    private TableView<ChangeRequestDto> requestsTable;
    @FXML
    private TableColumn<ChangeRequestDto, Long> requestIdColumn;
    @FXML
    private TableColumn<ChangeRequestDto, String> requestContractColumn;
    @FXML
    private TableColumn<ChangeRequestDto, String> requestTypeColumn;
    @FXML
    private TableColumn<ChangeRequestDto, String> requestInitiatorColumn;
    @FXML
    private TableColumn<ChangeRequestDto, String> requestProposalColumn;
    @FXML
    private TableColumn<ChangeRequestDto, String> requestCreatedColumn;

    private final ContractService service;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<ContractDto> contracts = FXCollections.observableArrayList();
    private final SortedList<ContractDto> sortedContracts = new SortedList<>(contracts);
    private final ObservableList<ChangeRequestDto> requests = FXCollections.observableArrayList();
    private ContractKind kind;

    public ContractListController(AppContext context, Navigator navigator) {
        this.service = context.contractService();
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        FormBindings.showWhen(busyIndicator, busy);
        configureContractColumns();
        configureRequestColumns();
        sortedContracts.comparatorProperty().bind(contractsTable.comparatorProperty());
        contractsTable.setItems(sortedContracts);
        requestsTable.setItems(requests);
        contractsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> refreshState());
        requestsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> refreshState());
        FormBindings.showWhen(detailsHint, contractsTable.getSelectionModel().selectedItemProperty().isNull());
        FormBindings.showWhen(detailsGrid, contractsTable.getSelectionModel().selectedItemProperty().isNotNull());
    }

    public void show(ContractKind kind) {
        this.kind = kind;
        counterpartyColumn.setText(kind.counterpartyTitle());
        detailsCounterpartyCaption.setText(kind.counterpartyTitle());
        boolean staff = service.isStaff();
        FormBindings.setPresent(offerButton, staff);
        FormBindings.setPresent(acceptButton, !staff);
        FormBindings.setPresent(rejectButton, !staff);
        FormBindings.setPresent(counterpartyColumn, staff);
        FormBindings.setPresent(forwarderColumn, !staff);
        refreshState();
        reload(null);
    }

    @FXML
    private void onRefresh() {
        ContractDto selected = contractsTable.getSelectionModel().getSelectedItem();
        reload(selected == null ? null : selected.id());
    }

    @FXML
    private void onOffer() {
        Background.run(busy,
                () -> new OfferData(service.counterparties(kind), service.ordersForOffer(kind)),
                this::openOfferForm,
                Dialogs::showError);
    }

    @FXML
    private void onAccept() {
        ContractDto selected = selectedContract();
        if (!service.canRespondToContract(selected)
                || !Dialogs.confirm("Подтвердить договор №" + selected.id() + " на сумму "
                + Formats.money(selected.price()) + " руб.?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.accept(kind, selected.id());
                    return selected.id();
                },
                id -> {
                    Dialogs.showSuccess("Договор №" + id + " подтверждён.");
                    reload(id);
                },
                Dialogs::showError);
    }

    @FXML
    private void onReject() {
        ContractDto selected = selectedContract();
        if (!service.canRespondToContract(selected)
                || !Dialogs.confirm("Отклонить договор №" + selected.id() + "?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.reject(kind, selected.id());
                    return selected.id();
                },
                this::reload,
                Dialogs::showError);
    }

    @FXML
    private void onAmend() {
        ContractDto selected = selectedContract();
        if (!service.canChange(selected, requests)) {
            return;
        }
        Navigator.LoadedView<ContractFormController> view = navigator.loadView(FORM_VIEW);
        ContractFormController form = view.controller();
        form.prepareAmendment(selected);
        FormDialog.show("Изменение договора №" + selected.id(), "Отправить запрос", view.root(),
                        form::validate, form::toAmendment)
                .ifPresent(amendment -> Background.run(busy,
                        () -> service.requestAmendment(kind, selected.id(), amendment),
                        created -> {
                            Dialogs.showSuccess("Запрос на изменение договора №" + selected.id()
                                    + " отправлен второй стороне.");
                            reload(selected.id());
                        },
                        Dialogs::showError));
    }

    @FXML
    private void onTerminate() {
        ContractDto selected = selectedContract();
        if (!service.canChange(selected, requests)
                || !Dialogs.confirm("Предложить расторгнуть договор №" + selected.id()
                + "? Договор будет расторгнут после согласия второй стороны.")) {
            return;
        }
        Background.run(busy,
                () -> service.requestTermination(kind, selected.id()),
                created -> {
                    Dialogs.showSuccess("Запрос на расторжение договора №" + selected.id()
                            + " отправлен второй стороне.");
                    reload(selected.id());
                },
                Dialogs::showError);
    }

    @FXML
    private void onRespondAccept() {
        ChangeRequestDto request = selectedRequest();
        if (!canRespond(request)
                || !Dialogs.confirm("Принять запрос №" + request.id() + " ("
                + request.type().toString().toLowerCase() + " договора №" + request.contractId() + ")?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.acceptRequest(kind, request.id());
                    return request.contractId();
                },
                id -> {
                    Dialogs.showSuccess("Запрос №" + request.id() + " принят.");
                    reload(id);
                },
                Dialogs::showError);
    }

    @FXML
    private void onRespondReject() {
        ChangeRequestDto request = selectedRequest();
        if (!canRespond(request) || !Dialogs.confirm("Отклонить запрос №" + request.id() + "?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.rejectRequest(kind, request.id());
                    return request.contractId();
                },
                this::reload,
                Dialogs::showError);
    }

    @FXML
    private void onCancelRequest() {
        ChangeRequestDto request = selectedRequest();
        if (!service.canCancelRequest(request) || !Dialogs.confirm("Отозвать свой запрос №" + request.id() + "?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    service.cancelRequest(kind, request.id());
                    return request.contractId();
                },
                this::reload,
                Dialogs::showError);
    }

    private void openOfferForm(OfferData data) {
        if (data.counterparties().isEmpty()) {
            Dialogs.showError("Нет доступных контрагентов: " + kind.counterpartyTitle().toLowerCase() + ".");
            return;
        }
        if (data.orders().isEmpty()) {
            Dialogs.showError(kind == ContractKind.CUSTOMER
                    ? "Нет заявок в статусе «Новая», по которым можно предложить договор."
                    : "Нет заявок в статусе «Договор заключён», по которым можно предложить договор.");
            return;
        }
        Navigator.LoadedView<ContractFormController> view = navigator.loadView(FORM_VIEW);
        ContractFormController form = view.controller();
        form.prepareOffer(kind, data.counterparties(), data.orders());
        FormDialog.show("Предложить договор", "Предложить", view.root(), form::validate, form::toContractRequest)
                .ifPresent(request -> Background.run(busy,
                        () -> service.create(kind, request),
                        created -> {
                            Dialogs.showSuccess("Договор №" + created.id() + " предложен: "
                                    + created.counterpartyNickname() + " должен его подтвердить.");
                            reload(created.id());
                        },
                        Dialogs::showError));
    }

    private void reload(Long selectContractId) {
        Background.run(busy,
                () -> new Snapshot(service.list(kind), service.openRequests(kind)),
                snapshot -> {
                    if (contractsTable.getScene() == null) {
                        return;
                    }
                    contracts.setAll(snapshot.contracts());
                    requests.setAll(snapshot.requests());
                    countLabel.setText("Договоров: " + snapshot.contracts().size()
                            + ", открытых запросов: " + snapshot.requests().size());
                    if (selectContractId != null) {
                        contracts.stream().filter(contract -> contract.id().equals(selectContractId))
                                .findFirst()
                                .ifPresent(contract -> contractsTable.getSelectionModel().select(contract));
                    }
                    refreshState();
                },
                Dialogs::showError);
    }

    private void refreshState() {
        ContractDto contract = selectedContract();
        ChangeRequestDto request = selectedRequest();
        acceptButton.setDisable(!service.canRespondToContract(contract));
        rejectButton.setDisable(!service.canRespondToContract(contract));
        boolean delivered = contract != null && contract.orderStatus() == OrderStatus.DELIVERED;
        FormBindings.setPresent(amendButton, !delivered);
        FormBindings.setPresent(terminateButton, !delivered);
        amendButton.setDisable(!service.canChange(contract, requests));
        terminateButton.setDisable(!service.canChange(contract, requests));
        respondAcceptButton.setDisable(!canRespond(request));
        respondRejectButton.setDisable(!canRespond(request));
        cancelRequestButton.setDisable(!service.canCancelRequest(request));
        if (contract == null) {
            return;
        }
        detailsNumber.setText("№" + contract.id());
        detailsStatus.setText(contract.status().toString());
        detailsOrder.setText("№" + contract.orderId());
        detailsCounterparty.setText(Formats.text(contract.counterpartyNickname()));
        detailsForwarder.setText(Formats.text(contract.forwarderNickname()));
        detailsPrice.setText(Formats.money(contract.price()) + " руб.");
        detailsTerms.setText(Formats.text(contract.terms()));
        detailsPeriod.setText(period(contract));
        detailsCreated.setText(Formats.dateTime(contract.createdAt()));
    }

    private boolean canRespond(ChangeRequestDto request) {
        return request != null && service.canRespondToRequest(request, contractOf(request));
    }

    private ContractDto contractOf(ChangeRequestDto request) {
        return contracts.stream().filter(contract -> contract.id().equals(request.contractId()))
                .findFirst().orElse(null);
    }

    private ContractDto selectedContract() {
        return contractsTable.getSelectionModel().getSelectedItem();
    }

    private ChangeRequestDto selectedRequest() {
        return requestsTable.getSelectionModel().getSelectedItem();
    }

    private void configureContractColumns() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().id()));
        orderColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("№" + cell.getValue().orderId()));
        counterpartyColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Formats.text(cell.getValue().counterpartyNickname())));
        forwarderColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Formats.text(cell.getValue().forwarderNickname())));
        priceColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.money(cell.getValue().price())));
        statusColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().status().toString()));
        for (TableColumn<ContractDto, String> column : List.of(orderColumn, counterpartyColumn, forwarderColumn,
                priceColumn, statusColumn)) {
            column.setSortable(false);
        }
        idColumn.setSortType(TableColumn.SortType.DESCENDING);
        contractsTable.getSortOrder().add(idColumn);
    }

    private void configureRequestColumns() {
        requestIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().id()));
        requestContractColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper("№" + cell.getValue().contractId()));
        requestTypeColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().type().toString()));
        requestInitiatorColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Formats.text(cell.getValue().initiatorNickname())));
        requestProposalColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(proposal(cell.getValue())));
        requestCreatedColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Formats.dateTime(cell.getValue().createdAt())));
        for (TableColumn<ChangeRequestDto, String> column : List.of(requestContractColumn, requestTypeColumn,
                requestInitiatorColumn, requestProposalColumn, requestCreatedColumn)) {
            column.setSortable(false);
        }
    }

    private static String period(ContractDto contract) {
        if (contract.startDate() == null && contract.endDate() == null) {
            return Formats.text(null);
        }
        return Formats.date(contract.startDate()) + " — " + Formats.date(contract.endDate());
    }

    private static String proposal(ChangeRequestDto request) {
        if (request.type() == ChangeRequestType.TERMINATE) {
            return "Расторгнуть договор";
        }
        List<String> parts = new ArrayList<>();
        if (request.proposedPrice() != null) {
            parts.add("цена " + Formats.money(request.proposedPrice()) + " руб.");
        }
        if (request.proposedTerms() != null) {
            parts.add("условия: " + request.proposedTerms());
        }
        if (request.proposedStartDate() != null) {
            parts.add("начало " + Formats.date(request.proposedStartDate()));
        }
        if (request.proposedEndDate() != null) {
            parts.add("окончание " + Formats.date(request.proposedEndDate()));
        }
        return String.join("; ", parts);
    }
}
