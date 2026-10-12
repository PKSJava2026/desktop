package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.api.CooperationApi;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.request.ContractQuery;
import com.beta.expedition.desktop.model.request.CooperationContractRequest;
import com.beta.expedition.desktop.model.request.CooperationForm;
import com.beta.expedition.desktop.session.Session;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CooperationService {

    private final CooperationApi api;
    private final ContractApi contractApi;
    private final OrderService orderService;
    private final Session session;

    public CooperationService(CooperationApi api, ContractApi contractApi, OrderService orderService,
                              Session session) {
        this.api = api;
        this.contractApi = contractApi;
        this.orderService = orderService;
        this.session = session;
    }

    public boolean isCarrier() {
        return session.user().role() == Role.CARRIER;
    }

    public List<CooperationRequestDto> list() {
        return isCarrier() ? api.mine() : api.listNew();
    }

    public CooperationRequestDto create(CooperationForm form) {
        return api.create(form);
    }

    public void cancel(long id) {
        api.cancel(id);
    }

    public ContractDto createContract(long requestId, CooperationContractRequest request) {
        return api.createContract(requestId, request);
    }

    public List<OrderDto> ordersForContract() {
        Set<Long> withCarrier = contractApi.search(ContractKind.CARRIER, ContractQuery.all()).stream()
                .filter(contract -> contract.status() == ContractStatus.PENDING
                        || contract.status() == ContractStatus.ACTIVE)
                .map(ContractDto::orderId)
                .collect(Collectors.toSet());
        return orderService.list(false).stream()
                .filter(order -> order.status() == OrderStatus.CONTRACTED)
                .filter(order -> !withCarrier.contains(order.id()))
                .toList();
    }

    public boolean canCancel(CooperationRequestDto request) {
        return isCarrier() && request != null && request.status() == CooperationStatus.NEW;
    }

    public boolean canCreateContract(CooperationRequestDto request) {
        return !isCarrier() && request != null && request.status() == CooperationStatus.NEW;
    }
}
