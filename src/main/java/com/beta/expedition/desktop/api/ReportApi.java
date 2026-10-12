package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.StatItem;
import com.beta.expedition.desktop.model.TableDto;
import java.util.List;

public interface ReportApi {

    List<StatItem> statistics();

    List<String> tables();

    TableDto table(String name);

    byte[] exportXlsx();

    byte[] exportCsv();
}
