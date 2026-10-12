package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.request.OrderRequest;
import com.beta.expedition.desktop.service.OrderService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.FormDialog;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class OrdersController {

    private static final String FORM_VIEW = "/com/beta/expedition/desktop/view/order-form.fxml";

    @FXML
    private Label titleLabel;
    @FXML
    private CheckBox activeOnlyBox;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button statusButton;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private TableView<OrderDto> ordersTable;
    @FXML
    private TableColumn<OrderDto, Long> idColumn;
    @FXML
    private TableColumn<OrderDto, String> customerColumn;
    @FXML
    private TableColumn<OrderDto, String> cargoColumn;
    @FXML
    private TableColumn<OrderDto, String> originColumn;
    @FXML
    private TableColumn<OrderDto, String> destinationColumn;
    @FXML
    private TableColumn<OrderDto, BigDecimal> weightColumn;
    @FXML
    private TableColumn<OrderDto, BigDecimal> volumeColumn;
    @FXML
    private TableColumn<OrderDto, LocalDate> desiredDateColumn;
    @FXML
    private TableColumn<OrderDto, OrderStatus> statusColumn;
    @FXML
    private TableColumn<OrderDto, OffsetDateTime> createdColumn;
    @FXML
    private Label detailsHint;
    @FXML
    private GridPane detailsGrid;
    @FXML
    private Label detailsNumber;
    @FXML
    private Label detailsStatus;
    @FXML
    private Label detailsCustomerCaption;
    @FXML
    private Label detailsCustomer;
    @FXML
    private Label detailsCargo;
    @FXML
    private Label detailsOrigin;
    @FXML
    private Label detailsDestination;
    @FXML
    private Label detailsWeight;
    @FXML
    private Label detailsVolume;
    @FXML
    private Label detailsDesiredDate;
    @FXML
    private Label detailsCreated;

    private final OrderService orderService;
    private final Navigator navigator;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<OrderDto> orders = FXCollections.observableArrayList();

    public OrdersController(AppContext context, Navigator navigator) {
        this.orderService = context.orderService();
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        boolean customer = orderService.isCustomer();
        titleLabel.setText(customer ? "Мои заявки" : "Заявки на перевозку");
        setPresent(addButton, customer);
        setPresent(editButton, customer);
        setPresent(cancelButton, customer);
        setPresent(deleteButton, customer);
        setPresent(statusButton, !customer);
        setPresent(activeOnlyBox, !customer);
        setPresent(customerColumn, !customer);
        setPresent(detailsCustomerCaption, !customer);
        setPresent(detailsCustomer, !customer);

        FormBindings.showWhen(busyIndicator, busy);
        configureColumns();
        ordersTable.setItems(orders);
        ordersTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> showSelection(current));
        ordersTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && customer) {
                onEdit();
            }
        });
        FormBindings.showWhen(detailsHint, ordersTable.getSelectionModel().selectedItemProperty().isNull());
        FormBindings.showWhen(detailsGrid, ordersTable.getSelectionModel().selectedItemProperty().isNotNull());
        showSelection(null);
        reload(null);
    }

    @FXML
    private void onRefresh() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        reload(selected == null ? null : selected.id());
    }

    @FXML
    private void onAdd() {
        showForm(null).ifPresent(request -> Background.run(busy,
                () -> orderService.create(request),
                created -> reload(created.id()),
                Dialogs::showError));
    }

    @FXML
    private void onEdit() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        if (!orderService.canEdit(selected)) {
            return;
        }
        showForm(selected).ifPresent(request -> Background.run(busy,
                () -> orderService.update(selected.id(), request),
                updated -> reload(updated.id()),
                Dialogs::showError));
    }

    @FXML
    private void onCancelOrder() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        if (!orderService.canCancel(selected)
                || !Dialogs.confirm("Отменить заявку №" + selected.id() + "?")) {
            return;
        }
        Background.run(busy,
                () -> {
                    orderService.cancel(selected.id());
                    return selected.id();
                },
                this::reload,
                Dialogs::showError);
    }

    @FXML
    private void onDelete() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        if (!orderService.canDelete(selected)
                || !Dialogs.confirm("Удалить заявку №" + selected.id() + "? Это действие нельзя отменить.")) {
            return;
        }
        Background.run(busy,
                () -> {
                    orderService.delete(selected.id());
                    return selected.id();
                },
                id -> reload(null),
                Dialogs::showError);
    }

    @FXML
    private void onChangeStatus() {
        OrderDto selected = ordersTable.getSelectionModel().getSelectedItem();
        List<OrderStatus> options = orderService.nextStatuses(selected);
        if (options.isEmpty()) {
            return;
        }
        Dialogs.choose("Изменение статуса", "Новый статус заявки №" + selected.id(), options)
                .ifPresent(status -> Background.run(busy,
                        () -> {
                            orderService.changeStatus(selected.id(), status);
                            return selected.id();
                        },
                        this::reload,
                        Dialogs::showError));
    }

    @FXML
    private void onActiveOnlyChanged() {
        reload(null);
    }

    private void reload(Long selectId) {
        boolean activeOnly = activeOnlyBox.isSelected();
        Background.run(busy,
                () -> orderService.list(activeOnly),
                list -> {
                    orders.setAll(list);
                    if (selectId != null) {
                        orders.stream().filter(order -> order.id().equals(selectId)).findFirst()
                                .ifPresent(order -> {
                                    ordersTable.getSelectionModel().select(order);
                                    ordersTable.scrollTo(order);
                                });
                    }
                    showSelection(ordersTable.getSelectionModel().getSelectedItem());
                },
                Dialogs::showError);
    }

    private Optional<OrderRequest> showForm(OrderDto existing) {
        Navigator.LoadedView<OrderFormController> view = navigator.loadView(FORM_VIEW);
        OrderFormController form = view.controller();
        if (existing != null) {
            form.fill(existing);
        }
        String title = existing == null ? "Новая заявка" : "Заявка №" + existing.id();
        return FormDialog.show(title, "Сохранить", view.root(), form::validate, form::toRequest);
    }

    private void showSelection(OrderDto order) {
        editButton.setDisable(!orderService.canEdit(order));
        cancelButton.setDisable(!orderService.canCancel(order));
        deleteButton.setDisable(!orderService.canDelete(order));
        statusButton.setDisable(orderService.nextStatuses(order).isEmpty());
        if (order == null) {
            return;
        }
        detailsNumber.setText("№" + order.id());
        detailsStatus.setText(order.status().toString());
        detailsCustomer.setText(Formats.text(order.customerNickname()));
        detailsCargo.setText(Formats.text(order.cargoDescription()));
        detailsOrigin.setText(Formats.text(order.origin()));
        detailsDestination.setText(Formats.text(order.destination()));
        detailsWeight.setText(Formats.decimal(order.weightKg()) + (order.weightKg() == null ? "" : " кг"));
        detailsVolume.setText(Formats.decimal(order.volumeM3()) + (order.volumeM3() == null ? "" : " м³"));
        detailsDesiredDate.setText(Formats.date(order.desiredDate()));
        detailsCreated.setText(Formats.dateTime(order.createdAt()));
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().id()));
        customerColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().customerNickname()));
        cargoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().cargoDescription()));
        originColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().origin()));
        destinationColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().destination()));
        weightColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().weightKg()));
        volumeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().volumeM3()));
        desiredDateColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().desiredDate()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().status()));
        createdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().createdAt()));

        formatted(weightColumn, Formats::decimal);
        formatted(volumeColumn, Formats::decimal);
        formatted(desiredDateColumn, Formats::date);
        formatted(createdColumn, Formats::dateTime);
        idColumn.setSortType(TableColumn.SortType.DESCENDING);
        ordersTable.getSortOrder().add(idColumn);
    }

    private <T> void formatted(TableColumn<OrderDto, T> column, Function<T, String> formatter) {
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatter.apply(item));
            }
        });
    }

    private void setPresent(Node node, boolean present) {
        node.setVisible(present);
        node.setManaged(present);
    }

    private void setPresent(TableColumn<?, ?> column, boolean present) {
        column.setVisible(present);
    }
}
