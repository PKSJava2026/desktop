package com.beta.expedition.desktop.service;

import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.api.UserApi;
import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ChangeRequestStatus;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.AmendmentRequest;
import com.beta.expedition.desktop.model.request.ContractRequest;
import com.beta.expedition.desktop.session.Session;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContractService {

    private final ContractApi api;
    private final UserApi users;
    private final OrderService orders;
    private final Session session;

    public ContractService(ContractApi api, UserApi users, OrderService orders, Session session) {
        this.api = api;
        this.users = users;
        this.orders = orders;
        this.session = session;
    }

    public boolean isStaff() {
        Role role = session.user().role();
        return role == Role.FORWARDER || role == Role.ADMIN;
    }

    public List<ContractKind> kinds() {
        return switch (session.user().role()) {
            case CUSTOMER -> List.of(ContractKind.CUSTOMER);
            case CARRIER -> List.of(ContractKind.CARRIER);
            case FORWARDER, ADMIN -> List.of(ContractKind.CUSTOMER, ContractKind.CARRIER);
        };
    }

    public List<ContractDto> list(ContractKind kind) {
        return api.mine(kind);
    }

    public List<ChangeRequestDto> openRequests(ContractKind kind) {
        return api.pendingRequests(kind);
    }

    public ContractDto create(ContractKind kind, ContractRequest request) {
        return api.create(kind, request);
    }

    public List<UserDto> counterparties(ContractKind kind) {
        return users.list(kind == ContractKind.CUSTOMER ? Role.CUSTOMER : Role.CARRIER);
    }

    public List<OrderDto> ordersForOffer(ContractKind kind) {
        Set<Long> withOpenContract = api.mine(kind).stream()
                .filter(contract -> contract.status() == ContractStatus.PENDING
                        || contract.status() == ContractStatus.ACTIVE)
                .map(ContractDto::orderId)
                .collect(Collectors.toSet());
        OrderStatus required = kind == ContractKind.CUSTOMER ? OrderStatus.NEW : OrderStatus.CONTRACTED;
        return orders.list(false).stream()
                .filter(order -> order.status() == required)
                .filter(order -> !withOpenContract.contains(order.id()))
                .toList();
    }

    public void accept(ContractKind kind, long id) {
        api.accept(kind, id);
    }

    public void reject(ContractKind kind, long id) {
        api.reject(kind, id);
    }

    public ChangeRequestDto requestAmendment(ContractKind kind, long contractId, AmendmentRequest request) {
        return api.requestAmendment(kind, contractId, request);
    }

    public ChangeRequestDto requestTermination(ContractKind kind, long contractId) {
        return api.requestTermination(kind, contractId);
    }

    public void acceptRequest(ContractKind kind, long requestId) {
        api.acceptRequest(kind, requestId);
    }

    public void rejectRequest(ContractKind kind, long requestId) {
        api.rejectRequest(kind, requestId);
    }

    public void cancelRequest(ContractKind kind, long requestId) {
        api.cancelRequest(kind, requestId);
    }

    public boolean canRespondToContract(ContractDto contract) {
        return contract != null && !isStaff() && contract.status() == ContractStatus.PENDING
                && contract.counterpartyId().equals(session.user().id());
    }

    public boolean canChange(ContractDto contract, List<ChangeRequestDto> openRequests) {
        return contract != null && contract.status() == ContractStatus.ACTIVE
                && contract.orderStatus() != OrderStatus.DELIVERED
                && contract.orderStatus() != OrderStatus.IN_TRANSIT
                && openRequests.stream().noneMatch(request -> isFor(request, contract));
    }

    public boolean canRespondToRequest(ChangeRequestDto request, ContractDto contract) {
        if (request == null || contract == null || request.status() != ChangeRequestStatus.PENDING) {
            return false;
        }
        boolean initiatedByCounterparty = request.initiatedBy().equals(contract.counterpartyId());
        return isStaff() ? initiatedByCounterparty
                : !initiatedByCounterparty && contract.counterpartyId().equals(session.user().id());
    }

    public boolean canCancelRequest(ChangeRequestDto request) {
        return request != null && request.status() == ChangeRequestStatus.PENDING
                && request.initiatedBy().equals(session.user().id());
    }

    private boolean isFor(ChangeRequestDto request, ContractDto contract) {
        return request.contractId().equals(contract.id()) && request.status() == ChangeRequestStatus.PENDING;
    }
}
