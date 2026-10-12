package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.request.OrderQuery;
import com.beta.expedition.desktop.model.request.OrderRequest;
import java.util.List;

public interface OrderApi {

    OrderDto create(OrderRequest request);

    List<OrderDto> mine();

    OrderDto get(long id);

    OrderDto update(long id, OrderRequest request);

    void cancel(long id);

    void delete(long id);

    List<OrderDto> search(OrderQuery query);

    void changeStatus(long id, OrderStatus status);
}
