package com.beta.expedition.desktop.ui;

import com.beta.expedition.desktop.model.Role;

import java.util.Set;

public record Section(String title, String viewPath, Set<Role> roles) {
}
