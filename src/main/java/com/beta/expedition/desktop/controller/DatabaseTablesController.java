package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.TableDto;
import com.beta.expedition.desktop.service.TablesService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class DatabaseTablesController {

    @FXML
    private ComboBox<String> tableCombo;
    @FXML
    private TableView<List<Object>> dataTable;
    @FXML
    private Label countLabel;
    @FXML
    private ProgressIndicator busyIndicator;

    private final TablesService service;
    private final ObservableList<List<Object>> rows = FXCollections.observableArrayList();
    private final BooleanProperty busy = new SimpleBooleanProperty();

    public DatabaseTablesController(AppContext context, Navigator navigator) {
        this.service = new TablesService(context.api().reports());
    }

    @FXML
    private void initialize() {
        dataTable.setItems(rows);
        dataTable.setPlaceholder(new Label("В таблице нет записей"));
        FormBindings.showWhen(busyIndicator, busy);
        tableCombo.disableProperty().bind(busy);
        tableCombo.valueProperty().addListener((observable, previous, current) -> loadSelected());

        Background.run(busy, service::names, names -> {
            tableCombo.getItems().setAll(names);
            tableCombo.getSelectionModel().selectFirst();
        }, Dialogs::showError);
    }

    private void loadSelected() {
        String name = tableCombo.getValue();
        if (name == null) {
            return;
        }
        Background.run(busy, () -> service.read(name), this::show, Dialogs::showError);
    }

    private void show(TableDto table) {
        dataTable.getColumns().clear();
        for (int index = 0; index < table.columns().size(); index++) {
            int columnIndex = index;
            TableColumn<List<Object>, String> column = new TableColumn<>(table.columns().get(index));
            column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(format(cell.getValue(), columnIndex)));
            column.setSortable(false);
            dataTable.getColumns().add(column);
        }
        rows.setAll(table.rows());
        countLabel.setText("Записей: " + table.rows().size());
    }

    private static String format(List<Object> row, int index) {
        Object value = index < row.size() ? row.get(index) : null;
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean flag) {
            return flag ? "Да" : "Нет";
        }
        return value.toString();
    }
}
