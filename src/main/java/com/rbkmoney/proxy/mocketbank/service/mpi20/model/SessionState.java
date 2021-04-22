package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionState {
    private String transactionId;
    private Mpi20State state;
    private String terminationUri;
}
