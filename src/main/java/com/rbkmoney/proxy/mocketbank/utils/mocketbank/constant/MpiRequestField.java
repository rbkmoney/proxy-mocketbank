package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpiRequestField {

    PAN("pan"),
    YEAR("year"),
    PARES("paRes"),
    MONTH("month");

    private final String value;
}
