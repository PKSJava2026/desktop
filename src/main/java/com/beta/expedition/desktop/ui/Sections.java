package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.model.Role;

import java.util.EnumSet;
import java.util.List;

public final class Sections {

    private static final String VIEW_DIR = "/com/beta/expedition/desktop/view/";

    private static final List<Section> ALL = List.of(
            new Section("Профиль", VIEW_DIR + "profile.fxml", EnumSet.allOf(Role.class))
    );

    private Sections() {
    }

    public static List<Section> forRole(Role role) {
        return ALL.stream().filter(section -> section.roles().contains(role)).toList();
    }
}
