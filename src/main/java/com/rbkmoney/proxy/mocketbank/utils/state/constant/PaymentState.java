package com.rbkmoney.proxy.mocketbank.utils.state.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentState {

    PROCESSED("processed"),
    CAPTURED("captured"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    CONFIRM("confirm");

    private final String state;

}
