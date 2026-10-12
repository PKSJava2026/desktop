package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.ReportApi;
import com.beta.expedition.desktop.model.TableDto;

import java.util.List;

public class TablesService {

    private final ReportApi reports;

    public TablesService(ReportApi reports) {
        this.reports = reports;
    }

    public List<String> names() {
        return reports.tables();
    }

    public TableDto read(String name) {
        return reports.table(name);
    }
}
