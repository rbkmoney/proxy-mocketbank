package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;

import lombok.Data;

@Data
public class VerifyEnrollmentRequest {
    private String pan;
    private String year;
    private String month;
}
