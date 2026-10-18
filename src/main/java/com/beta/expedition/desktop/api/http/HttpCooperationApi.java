package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.ApiException;
import com.beta.expedition.desktop.api.CooperationApi;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.request.ContractRequest;
import com.beta.expedition.desktop.model.request.CooperationContractRequest;
import com.beta.expedition.desktop.model.request.CooperationForm;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

final class HttpCooperationApi implements CooperationApi {

    private static final TypeReference<List<CooperationRequestDto>> REQUESTS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpCooperationApi(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public CooperationRequestDto create(CooperationForm form) {
        return transport.post("/api/cooperation-requests", form, CooperationRequestDto.class);
    }

    @Override
    public List<CooperationRequestDto> mine() {
        return transport.get("/api/cooperation-requests/my", REQUESTS);
    }

    @Override
    public void cancel(long id) {
        transport.post("/api/cooperation-requests/" + id + "/cancel", null, Void.class);
    }

    @Override
    public List<CooperationRequestDto> listNew() {
        return transport.get("/api/cooperation-requests", new Query().add("status", "NEW"), REQUESTS);
    }

    @Override
    public ContractDto createContract(long requestId, CooperationContractRequest request) {
        CooperationRequestDto target = listNew().stream()
                .filter(r -> r.id() == requestId)
                .findFirst()
                .orElseThrow(() -> new ApiException("Заявка не найдена или уже обработана"));
        ContractRequest body = new ContractRequest(request.orderId(), target.carrierId(), request.price(),
                request.terms(), request.startDate(), request.endDate());
        return transport.post("/api/cooperation-requests/" + requestId + "/contract", body, ContractDto.class);
    }
}
