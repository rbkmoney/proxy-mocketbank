package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class AuthenticationRequest {
    private String threeDSServerTransID;

    @ToString.Exclude
    private String pan;
    private String cardholderName;
    @ToString.Exclude
    private String expDate;

    private String notificationUrl;
    private String amount;
    private String currency;
    private String terminationUri;

}
