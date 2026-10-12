package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ChangeRequestStatus;
import com.beta.expedition.desktop.model.ChangeRequestType;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractSort;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.AmendmentRequest;
import com.beta.expedition.desktop.model.request.ContractQuery;
import com.beta.expedition.desktop.model.request.ContractRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

final class MockContractApi implements ContractApi {

    private static final BigDecimal MAX_PRICE = new BigDecimal("10000000000");

    private enum Side {
        FORWARDER,
        COUNTERPARTY
    }

    private final MockStore store;
    private final MockSession session;

    MockContractApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public ContractDto create(ContractKind kind, ContractRequest request) {
        UserDto forwarder = session.requireStaff();
        if (request == null || request.orderId() == null || request.counterpartyId() == null) {
            throw MockErrors.invalid("Выберите заявку и контрагента");
        }
        return createContract(kind, forwarder.id(), request.orderId(), request.counterpartyId(), request.price(),
                request.terms(), request.startDate(), request.endDate());
    }

    ContractDto createContract(ContractKind kind, long forwarderId, long orderId, long counterpartyId,
                               BigDecimal price, String terms, LocalDate startDate, LocalDate endDate) {
        validate(price, startDate, endDate);
        OrderDto order = store.order(orderId);
        if (kind == ContractKind.CUSTOMER) {
            if (order.customerId() != counterpartyId) {
                throw MockErrors.conflict("Заявка #" + orderId + " принадлежит другому заказчику");
            }
            requireOrderStatus(order, OrderStatus.NEW);
        } else {
            UserDto carrier = store.users.get(counterpartyId);
            if (carrier == null || carrier.role() != Role.CARRIER || !carrier.active()) {
                throw MockErrors.conflict("Пользователь #" + counterpartyId
                        + " не является активным перевозчиком");
            }
            requireOrderStatus(order, OrderStatus.CONTRACTED);
        }
        boolean hasOpenContract = store.contracts.get(kind).values().stream()
                .anyMatch(c -> c.orderId() == orderId
                        && (c.status() == ContractStatus.PENDING || c.status() == ContractStatus.ACTIVE));
        if (hasOpenContract) {
            throw MockErrors.conflict("По заявке #" + orderId
                    + " уже есть действующий или ожидающий подтверждения договор");
        }
        ContractDto contract = store.addContract(kind, orderId, counterpartyId, forwarderId, price,
                blankToNull(terms), startDate, endDate, ContractStatus.PENDING, OffsetDateTime.now());
        store.notify(counterpartyId, "Экспедитор " + store.nickname(forwarderId) + " предлагает договор #"
                + contract.id() + " по заявке #" + orderId + ", цена " + price.toPlainString()
                + ". Подтвердите или отклоните его в разделе договоров", null);
        return contract;
    }

    @Override
    public List<ContractDto> search(ContractKind kind, ContractQuery query) {
        session.requireStaff();
        ContractQuery q = query == null ? ContractQuery.all() : query;
        if (q.from() != null && q.to() != null && q.to().isBefore(q.from())) {
            throw MockErrors.invalid("Конец периода раньше его начала");
        }
        List<ContractDto> found = store.contracts.get(kind).values().stream()
                .filter(c -> q.status() == null || c.status() == q.status())
                .filter(c -> q.from() == null || !c.createdAt().toLocalDate().isBefore(q.from()))
                .filter(c -> q.to() == null || !c.createdAt().toLocalDate().isAfter(q.to()))
                .filter(c -> q.date() == null || c.createdAt().toLocalDate().equals(q.date()))
                .filter(c -> q.orderId() == null || c.orderId().equals(q.orderId()))
                .toList();
        if (q.sort() == null) {
            return found;
        }
        return found.stream().sorted(comparator(q.sort(), q.descending())).toList();
    }

    @Override
    public List<ContractDto> mine(ContractKind kind) {
        UserDto actor = session.current();
        Side side = side(actor, kind);
        return store.contracts.get(kind).values().stream()
                .filter(c -> side == Side.FORWARDER || c.counterpartyId().equals(actor.id()))
                .toList();
    }

    @Override
    public ContractDto get(ContractKind kind, long id) {
        return load(session.current(), kind, id);
    }

    @Override
    public void accept(ContractKind kind, long id) {
        UserDto actor = session.current();
        ContractDto contract = ownPending(actor, kind, id);
        if (kind == ContractKind.CUSTOMER) {
            store.setOrderStatus(store.order(contract.orderId()), OrderStatus.CONTRACTED);
        }
        store.contracts.get(kind).put(id, MockCopies.withStatus(contract, ContractStatus.ACTIVE));
        store.notify(contract.forwarderId(), kind.counterpartyTitle() + " " + actor.nickname()
                + " подтвердил договор #" + id, null);
    }

    @Override
    public void reject(ContractKind kind, long id) {
        UserDto actor = session.current();
        ContractDto contract = ownPending(actor, kind, id);
        store.contracts.get(kind).put(id, MockCopies.withStatus(contract, ContractStatus.REJECTED));
        store.notify(contract.forwarderId(), kind.counterpartyTitle() + " " + actor.nickname()
                + " отклонил договор #" + id, null);
    }

    @Override
    public ChangeRequestDto requestAmendment(ContractKind kind, long contractId, AmendmentRequest request) {
        UserDto actor = session.current();
        ContractDto contract = requireActive(load(actor, kind, contractId));
        if (request == null || (request.price() == null && blankToNull(request.terms()) == null
                && request.startDate() == null && request.endDate() == null)) {
            throw MockErrors.invalid("Укажите хотя бы одно новое значение");
        }
        validate(request.price() != null ? request.price() : contract.price(),
                request.startDate() != null ? request.startDate() : contract.startDate(),
                request.endDate() != null ? request.endDate() : contract.endDate());
        requireNoPending(kind, contractId);
        ChangeRequestDto created = store.addChangeRequest(kind, contractId, ChangeRequestType.AMEND, actor.id(),
                request.price(), blankToNull(request.terms()), request.startDate(), request.endDate());
        notifyOtherSide(contract, created, "предлагает изменить договор");
        return created;
    }

    @Override
    public ChangeRequestDto requestTermination(ContractKind kind, long contractId) {
        UserDto actor = session.current();
        ContractDto contract = requireActive(load(actor, kind, contractId));
        requireNoPending(kind, contractId);
        ChangeRequestDto created = store.addChangeRequest(kind, contractId, ChangeRequestType.TERMINATE,
                actor.id(), null, null, null, null);
        notifyOtherSide(contract, created, "предлагает расторгнуть договор");
        return created;
    }

    @Override
    public List<ChangeRequestDto> pendingRequests(ContractKind kind) {
        UserDto actor = session.current();
        Side side = side(actor, kind);
        return store.changeRequests.values().stream()
                .filter(r -> r.kind() == kind && r.status() == ChangeRequestStatus.PENDING)
                .filter(r -> side == Side.FORWARDER
                        || store.contract(kind, r.contractId()).counterpartyId().equals(actor.id()))
                .toList();
    }

    @Override
    public void acceptRequest(ContractKind kind, long requestId) {
        respond(kind, requestId, true);
    }

    @Override
    public void rejectRequest(ContractKind kind, long requestId) {
        respond(kind, requestId, false);
    }

    @Override
    public void cancelRequest(ContractKind kind, long requestId) {
        UserDto actor = session.current();
        side(actor, kind);
        ChangeRequestDto request = pendingRequest(kind, requestId);
        if (!request.initiatedBy().equals(actor.id())) {
            throw MockErrors.forbidden("Отозвать запрос может только его автор");
        }
        store.changeRequests.put(requestId, MockCopies.resolved(request, ChangeRequestStatus.CANCELLED));
        notifyOtherSide(store.contract(kind, request.contractId()), request, "отозвал запрос по договору");
    }

    private void respond(ContractKind kind, long requestId, boolean accept) {
        UserDto actor = session.current();
        ChangeRequestDto request = pendingRequest(kind, requestId);
        ContractDto contract = load(actor, kind, request.contractId());
        boolean initiatedByCounterparty = request.initiatedBy().equals(contract.counterpartyId());
        Side responder = initiatedByCounterparty ? Side.FORWARDER : Side.COUNTERPARTY;
        if (side(actor, kind) != responder) {
            throw MockErrors.forbidden("Запрос должна подтвердить другая сторона договора");
        }
        if (accept) {
            apply(request, requireActive(contract));
        }
        store.changeRequests.put(requestId, MockCopies.resolved(request,
                accept ? ChangeRequestStatus.ACCEPTED : ChangeRequestStatus.REJECTED));
        store.notify(request.initiatedBy(), "Ваш запрос #" + requestId + " по договору #" + contract.id()
                + (accept ? " принят" : " отклонён"), requestId);
    }

    private void apply(ChangeRequestDto request, ContractDto contract) {
        ContractKind kind = contract.kind();
        if (request.type() == ChangeRequestType.TERMINATE) {
            store.contracts.get(kind).put(contract.id(),
                    MockCopies.withStatus(contract, ContractStatus.TERMINATED));
            if (kind == ContractKind.CUSTOMER) {
                OrderDto order = store.order(contract.orderId());
                if (MockStore.canTransition(order.status(), OrderStatus.CANCELLED)) {
                    store.setOrderStatus(order, OrderStatus.CANCELLED);
                }
            }
            return;
        }
        BigDecimal price = request.proposedPrice() != null ? request.proposedPrice() : contract.price();
        String terms = request.proposedTerms() != null ? request.proposedTerms() : contract.terms();
        LocalDate start = request.proposedStartDate() != null ? request.proposedStartDate() : contract.startDate();
        LocalDate end = request.proposedEndDate() != null ? request.proposedEndDate() : contract.endDate();
        validate(price, start, end);
        store.contracts.get(kind).put(contract.id(), MockCopies.withTerms(contract, price, terms, start, end));
    }

    private Side side(UserDto actor, ContractKind kind) {
        return switch (actor.role()) {
            case FORWARDER, ADMIN -> Side.FORWARDER;
            case CUSTOMER -> {
                if (kind != ContractKind.CUSTOMER) {
                    throw MockErrors.forbidden();
                }
                yield Side.COUNTERPARTY;
            }
            case CARRIER -> {
                if (kind != ContractKind.CARRIER) {
                    throw MockErrors.forbidden();
                }
                yield Side.COUNTERPARTY;
            }
        };
    }

    private ContractDto load(UserDto actor, ContractKind kind, long id) {
        Side side = side(actor, kind);
        ContractDto contract = store.contract(kind, id);
        if (side == Side.COUNTERPARTY && !contract.counterpartyId().equals(actor.id())) {
            throw MockErrors.notFound("договор", id);
        }
        return contract;
    }

    private ContractDto ownPending(UserDto actor, ContractKind kind, long id) {
        if (side(actor, kind) != Side.COUNTERPARTY) {
            throw MockErrors.forbidden("Договор подтверждает вторая сторона, а не экспедитор");
        }
        ContractDto contract = load(actor, kind, id);
        if (contract.status() != ContractStatus.PENDING) {
            throw MockErrors.conflict("Договор #" + id + " не ожидает подтверждения (статус «"
                    + contract.status() + "»)");
        }
        return contract;
    }

    private ContractDto requireActive(ContractDto contract) {
        if (contract.status() != ContractStatus.ACTIVE) {
            throw MockErrors.conflict("Изменить или расторгнуть можно только действующий договор (статус «"
                    + contract.status() + "»)");
        }
        return contract;
    }

    private ChangeRequestDto pendingRequest(ContractKind kind, long requestId) {
        ChangeRequestDto request = store.changeRequests.get(requestId);
        if (request == null || request.kind() != kind) {
            throw MockErrors.notFound("запрос", requestId);
        }
        if (request.status() != ChangeRequestStatus.PENDING) {
            throw MockErrors.conflict("Запрос #" + requestId + " уже обработан (статус «"
                    + request.status() + "»)");
        }
        return request;
    }

    private void requireNoPending(ContractKind kind, long contractId) {
        boolean exists = store.changeRequests.values().stream()
                .anyMatch(r -> r.kind() == kind && r.contractId() == contractId
                        && r.status() == ChangeRequestStatus.PENDING);
        if (exists) {
            throw MockErrors.conflict("По договору #" + contractId + " уже есть неподтверждённый запрос");
        }
    }

    private void notifyOtherSide(ContractDto contract, ChangeRequestDto request, String action) {
        boolean initiatedByCounterparty = request.initiatedBy().equals(contract.counterpartyId());
        long recipient = initiatedByCounterparty ? contract.forwarderId() : contract.counterpartyId();
        String author = initiatedByCounterparty ? contract.kind().counterpartyTitle() : "Экспедитор";
        store.notify(recipient, author + " " + request.initiatorNickname() + " " + action + " #"
                + contract.id() + " (запрос #" + request.id() + ")", request.id());
    }

    private void requireOrderStatus(OrderDto order, OrderStatus required) {
        if (order.status() != required) {
            throw MockErrors.conflict("Договор можно заключить только по заявке в статусе «"
                    + required + "» (сейчас «" + order.status() + "»)");
        }
    }

    private void validate(BigDecimal price, LocalDate startDate, LocalDate endDate) {
        if (price == null || price.signum() <= 0) {
            throw MockErrors.invalid("Цена должна быть больше нуля");
        }
        if (price.stripTrailingZeros().scale() > 2 || price.compareTo(MAX_PRICE) >= 0) {
            throw MockErrors.invalid("Цена: не более двух знаков после запятой и меньше "
                    + MAX_PRICE.toPlainString());
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw MockErrors.invalid("Дата окончания раньше даты начала");
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static Comparator<ContractDto> comparator(ContractSort sort, boolean descending) {
        Comparator<ContractDto> ascending = switch (sort) {
            case DATE -> Comparator.comparing(ContractDto::createdAt);
            case STATUS -> Comparator.comparing(ContractDto::status);
            case PRICE -> Comparator.comparing(ContractDto::price);
        };
        return descending ? ascending.reversed() : ascending;
    }
}
