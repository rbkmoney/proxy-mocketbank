package com.rbkmoney.proxy.mocketbank.service.mpi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyEnrollmentResponse {

    @JsonProperty("enrolled")
    private String enrolled;

    @JsonProperty("paReq")
    private String paReq;

    @JsonProperty("acsUrl")
    private String acsUrl;

}
