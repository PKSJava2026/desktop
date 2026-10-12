package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.export.ExportFormat;
import com.beta.expedition.desktop.model.StatItem;
import com.beta.expedition.desktop.model.TableDto;
import com.beta.expedition.desktop.service.StatisticsService;
import com.beta.expedition.desktop.service.StatisticsService.Indicator;
import com.beta.expedition.desktop.service.StatisticsService.Statistics;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Background;
import com.beta.expedition.desktop.ui.Dialogs;
import com.beta.expedition.desktop.ui.ExportActions;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Formats;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatisticsController {

    private static final String SHEET_NAME = "Статистика";
    private static final int HEADER_HEIGHT = 34;

    @FXML
    private DatePicker fromPicker;
    @FXML
    private DatePicker toPicker;
    @FXML
    private Button applyButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button csvButton;
    @FXML
    private Button xlsxButton;
    @FXML
    private Label periodLabel;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private TableView<Indicator> ordersTable;
    @FXML
    private TableColumn<Indicator, String> ordersNameColumn;
    @FXML
    private TableColumn<Indicator, String> ordersValueColumn;
    @FXML
    private TableView<StatItem> companyTable;
    @FXML
    private TableColumn<StatItem, String> companyNameColumn;
    @FXML
    private TableColumn<StatItem, String> companyValueColumn;

    private final StatisticsService service;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObjectProperty<Statistics> current = new SimpleObjectProperty<>();
    private final ObservableList<Indicator> orderIndicators = FXCollections.observableArrayList();
    private final ObservableList<StatItem> companyItems = FXCollections.observableArrayList();
    private LocalDate shownFrom;
    private LocalDate shownTo;

    public StatisticsController(AppContext context, Navigator navigator) {
        this.service = context.statisticsService();
    }

    @FXML
    private void initialize() {
        ordersNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        ordersValueColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().display()));
        companyNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        companyValueColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().value()));
        for (TableColumn<?, ?> column : List.of(ordersNameColumn, ordersValueColumn,
                companyNameColumn, companyValueColumn)) {
            column.setSortable(false);
        }
        ordersTable.setItems(orderIndicators);
        companyTable.setItems(companyItems);
        fitHeight(ordersTable);
        fitHeight(companyTable);
        fromPicker.setEditable(false);
        toPicker.setEditable(false);

        FormBindings.showWhen(busyIndicator, busy);
        applyButton.disableProperty().bind(busy);
        resetButton.disableProperty().bind(busy);
        csvButton.disableProperty().bind(busy.or(current.isNull()));
        xlsxButton.disableProperty().bind(busy.or(current.isNull()));
        reload();
    }

    @FXML
    private void onApply() {
        reload();
    }

    @FXML
    private void onReset() {
        fromPicker.setValue(null);
        toPicker.setValue(null);
        reload();
    }

    @FXML
    private void onExportCsv() {
        export(ExportFormat.CSV);
    }

    @FXML
    private void onExportXlsx() {
        export(ExportFormat.XLSX);
    }

    private void fitHeight(TableView<?> table) {
        table.prefHeightProperty().bind(Bindings.size(table.getItems())
                .multiply(table.getFixedCellSize()).add(HEADER_HEIGHT));
    }

    private void export(ExportFormat format) {
        ExportActions.export(csvButton.getScene().getWindow(), format, "statistics",
                () -> exportTable(format == ExportFormat.XLSX), busy);
    }

    private void reload() {
        LocalDate from = fromPicker.getValue();
        LocalDate to = toPicker.getValue();
        Background.run(busy,
                () -> service.load(from, to),
                statistics -> {
                    shownFrom = from;
                    shownTo = to;
                    current.set(statistics);
                    orderIndicators.setAll(statistics.orders());
                    companyItems.setAll(statistics.company());
                    periodLabel.setText("Период: " + periodText());
                },
                Dialogs::showError);
    }

    private String periodText() {
        if (shownFrom == null && shownTo == null) {
            return "за всё время";
        }
        return (shownFrom == null ? "…" : Formats.date(shownFrom)) + " — "
                + (shownTo == null ? "…" : Formats.date(shownTo));
    }

    private TableDto exportTable(boolean numeric) {
        Statistics statistics = current.get();
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("Период заявок", periodText()));
        for (Indicator indicator : statistics.orders()) {
            Object value = numeric && indicator.value() != null ? indicator.value() : indicator.display();
            rows.add(List.of(indicator.name(), value));
        }
        for (StatItem item : statistics.company()) {
            boolean integer = numeric && item.value() != null && item.value().matches("\\d{1,18}");
            rows.add(List.of(item.name(), integer ? Long.valueOf(item.value()) : item.value()));
        }
        return new TableDto(SHEET_NAME, List.of("Показатель", "Значение"), rows);
    }
}
