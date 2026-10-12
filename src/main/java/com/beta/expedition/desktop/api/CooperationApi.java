package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.request.CooperationContractRequest;
import com.beta.expedition.desktop.model.request.CooperationForm;
import java.util.List;

public interface CooperationApi {

    CooperationRequestDto create(CooperationForm form);

    List<CooperationRequestDto> mine();

    void cancel(long id);

    List<CooperationRequestDto> listNew();

    ContractDto createContract(long requestId, CooperationContractRequest request);
}
