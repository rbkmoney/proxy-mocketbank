package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidatePaResResponse {

    @JsonProperty("eci")
    private String eci;

    @JsonProperty("cavv")
    private String cavv;

    @JsonProperty("cavvAlgorithm")
    private String cavvAlgorithm;

    @JsonProperty("txId")
    private String txId;

    @JsonProperty("txTime")
    private String txTime;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

}
