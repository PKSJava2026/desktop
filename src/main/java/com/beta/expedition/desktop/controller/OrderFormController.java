package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.request.OrderRequest;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderFormController {

    private static final int CARGO_MIN = 3;
    private static final int CARGO_MAX = 500;
    private static final int PLACE_MAX = 100;
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("1000000");
    private static final BigDecimal MAX_VOLUME = new BigDecimal("10000");

    @FXML
    private TextArea cargoField;
    @FXML
    private TextField originField;
    @FXML
    private TextField destinationField;
    @FXML
    private TextField weightField;
    @FXML
    private TextField volumeField;
    @FXML
    private DatePicker desiredDatePicker;
    @FXML
    private Label errorLabel;

    public OrderFormController(AppContext context, Navigator navigator) {
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
        desiredDatePicker.setEditable(false);
    }

    public void fill(OrderDto order) {
        cargoField.setText(order.cargoDescription());
        originField.setText(order.origin());
        destinationField.setText(order.destination());
        weightField.setText(order.weightKg() == null ? "" : order.weightKg().stripTrailingZeros().toPlainString());
        volumeField.setText(order.volumeM3() == null ? "" : order.volumeM3().stripTrailingZeros().toPlainString());
        desiredDatePicker.setValue(order.desiredDate());
    }

    public boolean validate() {
        String origin = originField.getText();
        String destination = destinationField.getText();
        Validator validator = new Validator()
                .required("Описание груза", cargoField.getText())
                .length("Описание груза", cargoField.getText(), CARGO_MIN, CARGO_MAX)
                .required("Откуда", origin)
                .length("Откуда", origin, 2, PLACE_MAX)
                .required("Куда", destination)
                .length("Куда", destination, 2, PLACE_MAX)
                .check(isBlank(origin) || isBlank(destination)
                                || !origin.trim().equalsIgnoreCase(destination.trim()),
                        "Пункты «Откуда» и «Куда» не должны совпадать.")
                .number("Вес, кг", weightField.getText())
                .positive("Вес, кг", weightField.getText())
                .range("Вес, кг", weightField.getText(), MIN_AMOUNT, MAX_WEIGHT)
                .scale("Вес, кг", weightField.getText(), 2)
                .number("Объём, м³", volumeField.getText())
                .positive("Объём, м³", volumeField.getText())
                .range("Объём, м³", volumeField.getText(), MIN_AMOUNT, MAX_VOLUME)
                .scale("Объём, м³", volumeField.getText(), 2)
                .notInPast("Желаемая дата", desiredDatePicker.getValue());
        errorLabel.setText(validator.message());
        return validator.isValid();
    }

    public OrderRequest toRequest() {
        BigDecimal weight = Validator.parseDecimal(weightField.getText()).orElse(null);
        BigDecimal volume = Validator.parseDecimal(volumeField.getText()).orElse(null);
        LocalDate date = desiredDatePicker.getValue();
        return new OrderRequest(cargoField.getText().trim(), weight, volume, originField.getText().trim(),
                destinationField.getText().trim(), date);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @FXML
    private void onClearDate() {
        desiredDatePicker.setValue(null);
    }
}
