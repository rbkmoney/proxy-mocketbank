package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreparationResponse {

    private String threeDSServerTransID;

    private Error error;

    private String protocolVersion;

    private String threeDSMethodURL;

    private String threeDSMethodData;
}
