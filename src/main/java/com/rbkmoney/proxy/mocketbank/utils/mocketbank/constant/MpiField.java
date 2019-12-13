package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpiField {

    MESSAGE_ID("messageId"),
    PA_REQ("PaReq"),
    PA_RES("PaRes"),
    PA_REQ_CREATION_TIME("paReqCreationTime"),
    ACS_URL("acsUrl"),
    ACCT_ID("acctId"),
    PURCHASE_XID("purchaseXId");

    private final String value;
}
