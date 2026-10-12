package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.request.AmendmentRequest;
import com.beta.expedition.desktop.model.request.ContractQuery;
import com.beta.expedition.desktop.model.request.ContractRequest;
import java.util.List;

public interface ContractApi {

    ContractDto create(ContractKind kind, ContractRequest request);

    List<ContractDto> search(ContractKind kind, ContractQuery query);

    List<ContractDto> mine(ContractKind kind);

    ContractDto get(ContractKind kind, long id);

    void accept(ContractKind kind, long id);

    void reject(ContractKind kind, long id);

    ChangeRequestDto requestAmendment(ContractKind kind, long contractId, AmendmentRequest request);

    ChangeRequestDto requestTermination(ContractKind kind, long contractId);

    List<ChangeRequestDto> pendingRequests(ContractKind kind);

    void acceptRequest(ContractKind kind, long requestId);

    void rejectRequest(ContractKind kind, long requestId);

    void cancelRequest(ContractKind kind, long requestId);
}
