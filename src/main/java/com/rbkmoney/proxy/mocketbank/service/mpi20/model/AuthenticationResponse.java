package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {

    private String threeDSServerTransID;

    @JsonUnwrapped
    private Error error;

    private String transStatus;

    private String acsUrl;

    private String creq;

}
