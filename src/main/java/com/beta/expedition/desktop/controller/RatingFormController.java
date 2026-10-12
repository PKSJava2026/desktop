package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.service.RatingService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.util.Map;

public class RatingFormController {

    private static final Map<Integer, String> SCORE_LABELS = Map.of(
            5, "5 — отлично",
            4, "4 — хорошо",
            3, "3 — удовлетворительно",
            2, "2 — плохо",
            1, "1 — очень плохо");

    @FXML
    private Label subjectLabel;
    @FXML
    private ComboBox<Integer> scoreCombo;
    @FXML
    private TextArea commentField;
    @FXML
    private Label errorLabel;

    private final RatingService service;

    public RatingFormController(AppContext context, Navigator navigator) {
        this.service = context.ratingService();
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
        for (int score = RatingService.MAX_SCORE; score >= RatingService.MIN_SCORE; score--) {
            scoreCombo.getItems().add(score);
        }
        scoreCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer score) {
                return score == null ? "" : SCORE_LABELS.get(score);
            }

            @Override
            public Integer fromString(String text) {
                return null;
            }
        });
    }

    public void prepare(OrderDto order) {
        subjectLabel.setText("Заявка №" + order.id() + ": оценка " + service.targetTitle());
    }

    public boolean validate() {
        Validator validator = new Validator()
                .requiredValue("Оценка", scoreCombo.getValue())
                .length("Комментарий", commentField.getText(), 0, RatingService.MAX_COMMENT_LENGTH);
        errorLabel.setText(validator.message());
        return validator.isValid();
    }

    public Rating toResult() {
        String comment = commentField.getText() == null ? "" : commentField.getText().trim();
        return new Rating(scoreCombo.getValue(), comment.isEmpty() ? null : comment);
    }

    public record Rating(int score, String comment) {
    }
}
