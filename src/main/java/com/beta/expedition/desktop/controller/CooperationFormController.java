package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.request.CooperationForm;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.FormBindings;
import com.beta.expedition.desktop.ui.Navigator;
import com.beta.expedition.desktop.ui.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class CooperationFormController {

    private static final int DESCRIPTION_MIN = 10;
    private static final int DESCRIPTION_MAX = 1000;
    private static final int VEHICLE_MIN = 3;
    private static final int VEHICLE_MAX = 500;

    @FXML
    private TextArea descriptionField;
    @FXML
    private TextArea vehicleField;
    @FXML
    private Label errorLabel;

    public CooperationFormController(AppContext context, Navigator navigator) {
    }

    @FXML
    private void initialize() {
        FormBindings.showWhenNotEmpty(errorLabel);
    }

    public boolean validate() {
        Validator validator = new Validator()
                .required("О себе", descriptionField.getText())
                .length("О себе", descriptionField.getText(), DESCRIPTION_MIN, DESCRIPTION_MAX)
                .required("Транспорт", vehicleField.getText())
                .length("Транспорт", vehicleField.getText(), VEHICLE_MIN, VEHICLE_MAX);
        errorLabel.setText(validator.message());
        return validator.isValid();
    }

    public CooperationForm toForm() {
        return new CooperationForm(descriptionField.getText().trim(), vehicleField.getText().trim());
    }
}
