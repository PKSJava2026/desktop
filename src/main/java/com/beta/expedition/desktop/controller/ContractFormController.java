package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.AmendmentRequest;
import com.beta.expedition.desktop.model.request.ContractRequest;
import com.beta.expedition.desktop.service.ContractRules;
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
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ContractFormController {

    @FXML
    private VBox offerBox;
    @FXML
    private Label counterpartyCaption;
    @FXML
    private ComboBox<UserDto> counterpartyBox;
    @FXML
    private ComboBox<OrderDto> orderBox;
    @FXML
    private Label amendHint;
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

    private ContractKind kind;
    private ContractDto amended;
    private List<OrderDto> candidateOrders = List.of();

    public ContractFormController(AppContext context, Navigator navigator) {
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
        startPicker.setEditable(false);
        endPicker.setEditable(false);
        counterpartyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserDto user) {
                return user == null ? "" : user.nickname() + " · " + user.email();
            }

            @Override
            public UserDto fromString(String text) {
                return null;
            }
        });
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
        counterpartyBox.valueProperty().addListener((observable, previous, current) -> updateOrders(current));
    }

    public void prepareOffer(ContractKind kind, List<UserDto> counterparties, List<OrderDto> orders) {
        this.kind = kind;
        this.candidateOrders = orders;
        counterpartyCaption.setText(kind.counterpartyTitle());
        counterpartyBox.setItems(FXCollections.observableArrayList(counterparties));
        FormBindings.setPresent(amendHint, false);
        updateOrders(null);
    }

    public void prepareAmendment(ContractDto contract) {
        this.amended = contract;
        FormBindings.setPresent(offerBox, false);
        FormBindings.setPresent(amendHint, true);
        priceField.setText(contract.price() == null ? "" : contract.price().toPlainString());
        termsField.setText(contract.terms() == null ? "" : contract.terms());
        startPicker.setValue(contract.startDate());
        endPicker.setValue(contract.endDate());
    }

    public boolean validate() {
        Validator validator = new Validator();
        if (amended == null) {
            validator.requiredValue(kind.counterpartyTitle(), counterpartyBox.getValue())
                    .requiredValue("Заявка на перевозку", orderBox.getValue());
            ContractRules.price(validator, priceField.getText(), true);
            ContractRules.terms(validator, termsField.getText());
            ContractRules.dates(validator, startPicker.getValue(), endPicker.getValue());
        } else {
            validateAmendment(validator);
        }
        errorLabel.setText(validator.message());
        return validator.isValid();
    }

    public ContractRequest toContractRequest() {
        return new ContractRequest(orderBox.getValue().id(), counterpartyBox.getValue().id(), price(),
                terms(), startPicker.getValue(), endPicker.getValue());
    }

    public AmendmentRequest toAmendment() {
        return new AmendmentRequest(
                priceChanged() ? price() : null,
                termsChanged() ? terms() : null,
                startChanged() ? startPicker.getValue() : null,
                endChanged() ? endPicker.getValue() : null);
    }

    @FXML
    private void onClearStart() {
        startPicker.setValue(null);
    }

    @FXML
    private void onClearEnd() {
        endPicker.setValue(null);
    }

    private void validateAmendment(Validator validator) {
        ContractRules.price(validator, priceField.getText(), false);
        ContractRules.terms(validator, termsField.getText());
        validator
                .notInPast("Дата начала", startChanged() ? startPicker.getValue() : null)
                .notInPast("Дата окончания", endChanged() ? endPicker.getValue() : null)
                .notBefore("Дата окончания", endPicker.getValue(), "Дата начала", startPicker.getValue());
        if (validator.isValid() && !(priceChanged() || termsChanged() || startChanged() || endChanged())) {
            validator.check(false, "Измените хотя бы одно значение: цену, условия или даты.");
        }
    }

    private void updateOrders(UserDto counterparty) {
        List<OrderDto> available = candidateOrders;
        if (kind == ContractKind.CUSTOMER) {
            available = counterparty == null ? List.of() : candidateOrders.stream()
                    .filter(order -> order.customerId().equals(counterparty.id()))
                    .toList();
        }
        orderBox.setItems(FXCollections.observableArrayList(available));
        orderBox.setValue(null);
        orderBox.setPromptText(kind == ContractKind.CUSTOMER && counterparty == null
                ? "Сначала выберите заказчика" : "Выберите заявку");
    }

    private BigDecimal price() {
        return Validator.parseDecimal(priceField.getText()).orElse(null);
    }

    private String terms() {
        String text = termsField.getText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private boolean priceChanged() {
        BigDecimal entered = price();
        return entered != null && amended != null && entered.compareTo(amended.price()) != 0;
    }

    private boolean termsChanged() {
        return terms() != null && amended != null && !Objects.equals(terms(), amended.terms());
    }

    private boolean startChanged() {
        return startPicker.getValue() != null && amended != null
                && !Objects.equals(startPicker.getValue(), amended.startDate());
    }

    private boolean endChanged() {
        return endPicker.getValue() != null && amended != null
                && !Objects.equals(endPicker.getValue(), amended.endDate());
    }
}
