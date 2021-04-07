package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultRequest {
    private String threeDSServerTransID;
}