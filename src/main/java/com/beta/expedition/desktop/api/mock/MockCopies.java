package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ChangeRequestStatus;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.NotificationDto;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.UserDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

final class MockCopies {

    private MockCopies() {
    }

    static OrderDto withStatus(OrderDto order, OrderStatus status) {
        return new OrderDto(order.id(), order.customerId(), order.customerNickname(), order.cargoDescription(),
                order.weightKg(), order.volumeM3(), order.origin(), order.destination(), order.desiredDate(),
                status, order.createdAt());
    }

    static ContractDto withStatus(ContractDto contract, ContractStatus status) {
        return new ContractDto(contract.id(), contract.kind(), contract.orderId(), contract.counterpartyId(),
                contract.counterpartyNickname(), contract.forwarderId(), contract.forwarderNickname(),
                contract.price(), contract.terms(), contract.startDate(), contract.endDate(), status,
                contract.createdAt());
    }

    static ContractDto withTerms(ContractDto contract, BigDecimal price, String terms, LocalDate startDate,
                                 LocalDate endDate) {
        return new ContractDto(contract.id(), contract.kind(), contract.orderId(), contract.counterpartyId(),
                contract.counterpartyNickname(), contract.forwarderId(), contract.forwarderNickname(),
                price, terms, startDate, endDate, contract.status(), contract.createdAt());
    }

    static ChangeRequestDto resolved(ChangeRequestDto request, ChangeRequestStatus status) {
        return new ChangeRequestDto(request.id(), request.kind(), request.contractId(), request.type(),
                request.initiatedBy(), request.initiatorNickname(), request.proposedPrice(),
                request.proposedTerms(), request.proposedStartDate(), request.proposedEndDate(), status,
                request.createdAt(), OffsetDateTime.now());
    }

    static CooperationRequestDto withStatus(CooperationRequestDto request, CooperationStatus status) {
        return new CooperationRequestDto(request.id(), request.carrierId(), request.carrierNickname(),
                request.description(), request.vehicleInfo(), status, request.createdAt());
    }

    static NotificationDto asRead(NotificationDto notification) {
        return new NotificationDto(notification.id(), notification.message(), notification.changeRequestId(),
                true, notification.createdAt());
    }

    static UserDto deactivated(UserDto user) {
        return new UserDto(user.id(), user.nickname(), user.email(), user.role(), false, user.createdAt());
    }
}
