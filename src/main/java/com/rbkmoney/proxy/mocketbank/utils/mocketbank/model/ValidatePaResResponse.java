package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidatePaResResponse {

    @JsonProperty("eci")
    private String eci;

    @JsonProperty("cavv")
    private String cavv;

    @Override
    public String toString() {
        return "ValidatePaResResponse{" +
                "eci='" + eci + '\'' +
                ", cavv='" + cavv + '\'' +
                ", cavvAlgorithm='" + cavvAlgorithm + '\'' +
                ", txId='" + txId + '\'' +
                ", txTime='" + txTime + '\'' +
                ", transactionStatus='" + transactionStatus + '\'' +
                '}';
    }

    @JsonProperty("cavvAlgorithm")
    private String cavvAlgorithm;

    @JsonProperty("txId")
    private String txId;

    @JsonProperty("txTime")
    private String txTime;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    public String getEci() {
        return eci;
    }

    public void setEci(String eci) {
        this.eci = eci;
    }

    public String getCavv() {
        return cavv;
    }

    public void setCavv(String cavv) {
        this.cavv = cavv;
    }

    public String getCavvAlgorithm() {
        return cavvAlgorithm;
    }

    public void setCavvAlgorithm(String cavvAlgorithm) {
        this.cavvAlgorithm = cavvAlgorithm;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxTime() {
        return txTime;
    }

    public void setTxTime(String txTime) {
        this.txTime = txTime;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
