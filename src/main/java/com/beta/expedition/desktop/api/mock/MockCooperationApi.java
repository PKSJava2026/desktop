package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.CooperationApi;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.CooperationContractRequest;
import com.beta.expedition.desktop.model.request.CooperationForm;
import java.time.OffsetDateTime;
import java.util.List;

final class MockCooperationApi implements CooperationApi {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_VEHICLE_LENGTH = 500;

    private final MockStore store;
    private final MockSession session;
    private final MockContractApi contracts;

    MockCooperationApi(MockStore store, MockSession session, MockContractApi contracts) {
        this.store = store;
        this.session = session;
        this.contracts = contracts;
    }

    @Override
    public CooperationRequestDto create(CooperationForm form) {
        UserDto carrier = session.require(Role.CARRIER);
        if (form == null || form.description() == null || form.description().isBlank()) {
            throw MockErrors.invalid("Расскажите о себе: описание обязательно");
        }
        if (form.vehicleInfo() == null || form.vehicleInfo().isBlank()) {
            throw MockErrors.invalid("Информация о транспорте обязательна");
        }
        if (form.description().length() > MAX_DESCRIPTION_LENGTH
                || form.vehicleInfo().length() > MAX_VEHICLE_LENGTH) {
            throw MockErrors.invalid("Описание не длиннее " + MAX_DESCRIPTION_LENGTH
                    + " символов, транспорт не длиннее " + MAX_VEHICLE_LENGTH);
        }
        store.cooperationRequests.values().stream()
                .filter(r -> r.carrierId().equals(carrier.id()) && r.status() == CooperationStatus.NEW)
                .findFirst()
                .ifPresent(r -> {
                    throw MockErrors.conflict("У вас уже есть необработанная заявка #" + r.id());
                });
        return store.addCooperationRequest(carrier.id(), form.description().trim(), form.vehicleInfo().trim(),
                CooperationStatus.NEW, OffsetDateTime.now());
    }

    @Override
    public List<CooperationRequestDto> mine() {
        UserDto carrier = session.require(Role.CARRIER);
        return store.cooperationRequests.values().stream()
                .filter(r -> r.carrierId().equals(carrier.id()))
                .toList();
    }

    @Override
    public void cancel(long id) {
        UserDto carrier = session.require(Role.CARRIER);
        CooperationRequestDto request = store.cooperationRequest(id);
        if (!request.carrierId().equals(carrier.id())) {
            throw MockErrors.notFound("заявка о сотрудничестве", id);
        }
        requireNew(request);
        store.cooperationRequests.put(id, MockCopies.withStatus(request, CooperationStatus.CANCELLED));
    }

    @Override
    public List<CooperationRequestDto> listNew() {
        session.requireStaff();
        return store.cooperationRequests.values().stream()
                .filter(r -> r.status() == CooperationStatus.NEW)
                .toList();
    }

    @Override
    public ContractDto createContract(long requestId, CooperationContractRequest request) {
        UserDto forwarder = session.requireStaff();
        CooperationRequestDto cooperation = store.cooperationRequest(requestId);
        requireNew(cooperation);
        if (request == null || request.orderId() == null) {
            throw MockErrors.invalid("Выберите заявку на перевозку");
        }
        ContractDto contract = contracts.createContract(ContractKind.CARRIER, forwarder.id(), request.orderId(),
                cooperation.carrierId(), request.price(), request.terms(), request.startDate(), request.endDate());
        store.carrierContractCooperation.put(contract.id(), requestId);
        store.cooperationRequests.put(requestId, MockCopies.withStatus(cooperation, CooperationStatus.PROCESSED));
        return contract;
    }

    private void requireNew(CooperationRequestDto request) {
        if (request.status() != CooperationStatus.NEW) {
            throw MockErrors.conflict("Заявка #" + request.id() + " уже обработана (статус «"
                    + request.status() + "»)");
        }
    }
}
