package com.rbkmoney.proxy.mocketbank.utils.mobilephone;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MobilePhone {
    private final String number;
    private final String operator;
    private final String action;
}
