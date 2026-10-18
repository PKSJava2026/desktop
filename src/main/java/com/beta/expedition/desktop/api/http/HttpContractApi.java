package com.beta.expedition.desktop.api.http;

import com.beta.expedition.desktop.api.ContractApi;
import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.request.AmendmentRequest;
import com.beta.expedition.desktop.model.request.ContractQuery;
import com.beta.expedition.desktop.model.request.ContractRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

final class HttpContractApi implements ContractApi {

    private static final TypeReference<List<ContractDto>> CONTRACTS = new TypeReference<>() {
    };
    private static final TypeReference<List<ChangeRequestDto>> REQUESTS = new TypeReference<>() {
    };

    private final HttpTransport transport;

    HttpContractApi(HttpTransport transport) {
        this.transport = transport;
    }

    private static String base(ContractKind kind) {
        return "/api/" + kind.pathSegment() + "-contracts";
    }

    @Override
    public ContractDto create(ContractKind kind, ContractRequest request) {
        return transport.post(base(kind), request, ContractDto.class);
    }

    @Override
    public List<ContractDto> search(ContractKind kind, ContractQuery query) {
        Query q = new Query()
                .add("status", query.status())
                .add("from", query.from())
                .add("to", query.to())
                .add("date", query.date())
                .add("orderId", query.orderId())
                .add("sort", query.sort())
                .add("desc", query.descending() ? "true" : null);
        return transport.get(base(kind), q, CONTRACTS);
    }

    @Override
    public List<ContractDto> mine(ContractKind kind) {
        return transport.get(base(kind) + "/my", CONTRACTS);
    }

    @Override
    public ContractDto get(ContractKind kind, long id) {
        return transport.get(base(kind) + "/" + id, ContractDto.class);
    }

    @Override
    public void accept(ContractKind kind, long id) {
        transport.post(base(kind) + "/" + id + "/accept", null, Void.class);
    }

    @Override
    public void reject(ContractKind kind, long id) {
        transport.post(base(kind) + "/" + id + "/reject", null, Void.class);
    }

    @Override
    public ChangeRequestDto requestAmendment(ContractKind kind, long contractId, AmendmentRequest request) {
        return transport.post(base(kind) + "/" + contractId + "/amendments", request, ChangeRequestDto.class);
    }

    @Override
    public ChangeRequestDto requestTermination(ContractKind kind, long contractId) {
        return transport.post(base(kind) + "/" + contractId + "/terminations", null, ChangeRequestDto.class);
    }

    @Override
    public List<ChangeRequestDto> pendingRequests(ContractKind kind) {
        return transport.get(base(kind) + "/requests", REQUESTS);
    }

    @Override
    public void acceptRequest(ContractKind kind, long requestId) {
        transport.post(base(kind) + "/requests/" + requestId + "/accept", null, Void.class);
    }

    @Override
    public void rejectRequest(ContractKind kind, long requestId) {
        transport.post(base(kind) + "/requests/" + requestId + "/reject", null, Void.class);
    }

    @Override
    public void cancelRequest(ContractKind kind, long requestId) {
        transport.post(base(kind) + "/requests/" + requestId + "/cancel", null, Void.class);
    }
}
