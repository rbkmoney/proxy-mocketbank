package com.rbkmoney.proxy.test.utils.testmpi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyEnrollmentResponse {

    @JsonProperty("enrolled")
    private String enrolled;

    @JsonProperty("paReq")
    private String paReq;

    @JsonProperty("acsUrl")
    private String acsUrl;

    public String getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(String enrolled) {
        this.enrolled = enrolled;
    }

    public String getPaReq() {
        return paReq;
    }

    public void setPaReq(String paReq) {
        this.paReq = paReq;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public void setAcsUrl(String acsUrl) {
        this.acsUrl = acsUrl;
    }

    @Override
    public String toString() {
        return "VerifyEnrollmentResponse{" +
                "enrolled='" + enrolled + '\'' +
                ", paReq='" + paReq + '\'' +
                ", acsUrl='" + acsUrl + '\'' +
                '}';
    }
}
