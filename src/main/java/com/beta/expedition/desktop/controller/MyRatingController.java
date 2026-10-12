package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.RatingSummaryDto;
import com.beta.expedition.desktop.service.RatingService;
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
import javafx.scene.control.TableView;

import java.util.Locale;

public class MyRatingController {

    @FXML
    private Label averageLabel;
    @FXML
    private Label starsLabel;
    @FXML
    private Label countLabel;
    @FXML
    private TableView<RatingDto> ratingsTable;
    @FXML
    private TableColumn<RatingDto, String> orderColumn;
    @FXML
    private TableColumn<RatingDto, String> fromColumn;
    @FXML
    private TableColumn<RatingDto, String> scoreColumn;
    @FXML
    private TableColumn<RatingDto, String> commentColumn;
    @FXML
    private TableColumn<RatingDto, String> dateColumn;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator busyIndicator;

    private final RatingService service;
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final ObservableList<RatingDto> ratings = FXCollections.observableArrayList();

    public MyRatingController(AppContext context, Navigator navigator) {
        this.service = context.ratingService();
    }

    @FXML
    private void initialize() {
        orderColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("№" + cell.getValue().orderId()));
        fromColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.text(cell.getValue().fromNickname())));
        scoreColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                Formats.stars(cell.getValue().score(), RatingService.MAX_SCORE)));
        commentColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.text(cell.getValue().comment())));
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(Formats.dateTime(cell.getValue().createdAt())));
        ratingsTable.setItems(ratings);

        FormBindings.showWhen(busyIndicator, busy);
        refreshButton.disableProperty().bind(busy);
        showSummary(new RatingSummaryDto(0, 0));
        reload();
    }

    @FXML
    private void onRefresh() {
        reload();
    }

    private void reload() {
        Background.run(busy, service::mine, overview -> {
            ratings.setAll(overview.ratings());
            showSummary(overview.summary());
        }, Dialogs::showError);
    }

    private void showSummary(RatingSummaryDto summary) {
        if (summary.count() == 0) {
            averageLabel.setText("—");
            starsLabel.setText(Formats.stars(0, RatingService.MAX_SCORE));
            countLabel.setText("Оценок пока нет");
            return;
        }
        averageLabel.setText(String.format(Locale.forLanguageTag("ru"), "%.2f из %d", summary.average(),
                RatingService.MAX_SCORE));
        starsLabel.setText(Formats.stars((int) Math.round(summary.average()), RatingService.MAX_SCORE));
        countLabel.setText("Оценок: " + summary.count());
    }
}
