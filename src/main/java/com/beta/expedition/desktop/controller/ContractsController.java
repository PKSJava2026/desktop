package com.beta.expedition.desktop.controller;

import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.service.ContractService;
import com.beta.expedition.desktop.ui.AppContext;
import com.beta.expedition.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.util.List;

public class ContractsController {

    private static final String LIST_VIEW = "/com/beta/expedition/desktop/view/contract-list.fxml";

    @FXML
    private StackPane contentPane;

    private final ContractService service;
    private final Navigator navigator;

    public ContractsController(AppContext context, Navigator navigator) {
        this.service = context.contractService();
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        List<ContractKind> kinds = service.kinds();
        if (kinds.size() == 1) {
            contentPane.getChildren().setAll(loadList(kinds.get(0)));
            return;
        }
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (ContractKind kind : kinds) {
            Tab tab = new Tab(kind.toString(), loadList(kind));
            tabs.getTabs().add(tab);
        }
        contentPane.getChildren().setAll(tabs);
    }

    private Parent loadList(ContractKind kind) {
        Navigator.LoadedView<ContractListController> view = navigator.loadView(LIST_VIEW);
        view.controller().show(kind);
        return view.root();
    }
}
