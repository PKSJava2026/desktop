package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.request.CooperationContractRequest;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;

public class CooperationContractFormController {

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private static final int TERMS_MAX = 1000;

    @FXML
    private Label carrierLabel;
    @FXML
    private ComboBox<OrderDto> orderBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea termsField;
    @FXML
    private DatePicker startPicker;
    @FXML
    private DatePicker endPicker;
    @FXML
    private Label errorLabel;

    public CooperationContractFormController(AppContext context, Navigator navigator) {
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
        startPicker.setEditable(false);
        endPicker.setEditable(false);
        orderBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(OrderDto order) {
                return order == null ? "" : "№" + order.id() + " · " + order.cargoDescription() + " · "
                        + order.origin() + " → " + order.destination();
            }

            @Override
            public OrderDto fromString(String text) {
                return null;
            }
        });
    }

    public void prepare(CooperationRequestDto request, List<OrderDto> orders) {
        carrierLabel.setText(request.carrierNickname());
        orderBox.setItems(FXCollections.observableArrayList(orders));
    }

    public boolean validate() {
        Validator validator = new Validator()
                .requiredValue("Заявка на перевозку", orderBox.getValue())
                .required("Цена", priceField.getText())
                .number("Цена", priceField.getText())
                .positive("Цена", priceField.getText())
                .range("Цена", priceField.getText(), MIN_PRICE, MAX_PRICE)
                .scale("Цена", priceField.getText(), 2)
                .length("Условия", termsField.getText(), 0, TERMS_MAX)
                .notInPast("Дата начала", startPicker.getValue())
                .notInPast("Дата окончания", endPicker.getValue())
                .notBefore("Дата окончания", endPicker.getValue(), "Дата начала", startPicker.getValue());
        errorLabel.setText(validator.message());
        return validator.isValid();
    }

    public CooperationContractRequest toRequest() {
        String terms = termsField.getText() == null || termsField.getText().isBlank()
                ? null : termsField.getText().trim();
        return new CooperationContractRequest(orderBox.getValue().id(),
                Validator.parseDecimal(priceField.getText()).orElse(null), terms,
                startPicker.getValue(), endPicker.getValue());
    }

    @FXML
    private void onClearStart() {
        startPicker.setValue(null);
    }

    @FXML
    private void onClearEnd() {
        endPicker.setValue(null);
    }
}
