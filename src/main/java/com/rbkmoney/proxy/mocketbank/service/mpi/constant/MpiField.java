package com.rbkmoney.proxy.mocketbank.service.mpi.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpiField {

    MESSAGE_ID("messageId"),
    PA_REQ("PaReq"),
    MD("MD"),
    PA_RES("PaRes"),
    TERM_URL("TermUrl"),
    PA_REQ_CREATION_TIME("paReqCreationTime"),
    ACS_URL("acsUrl"),
    ACCT_ID("acctId"),
    PURCHASE_XID("purchaseXId"),

    PAN("pan"),
    YEAR("year"),
    PARES("paRes"),
    MONTH("month");

    private final String value;
}
