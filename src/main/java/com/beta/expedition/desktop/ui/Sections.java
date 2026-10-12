package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.model.Role;

import java.util.EnumSet;
import java.util.List;

public final class Sections {

    private static final String VIEW_DIR = "/com/beta/expedition/desktop/view/";

    private static final List<Section> ALL = List.of(
            new Section("Заявки", VIEW_DIR + "orders.fxml", EnumSet.of(Role.CUSTOMER, Role.FORWARDER, Role.ADMIN)),
            new Section("Профиль", VIEW_DIR + "profile.fxml", EnumSet.allOf(Role.class)),
            new Section("Экспедиторы", VIEW_DIR + "forwarders.fxml", EnumSet.of(Role.ADMIN)),
            new Section("Таблицы БД", VIEW_DIR + "database-tables.fxml", EnumSet.of(Role.FORWARDER, Role.ADMIN))
    );

    private Sections() {
    }

    public static List<Section> forRole(Role role) {
        return ALL.stream().filter(section -> section.roles().contains(role)).toList();
    }
}
