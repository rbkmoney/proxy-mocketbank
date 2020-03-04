package com.rbkmoney.proxy.mocketbank.utils.constant.testcards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Maestro implements TestCard {
    SUCCESS("586824160825533338");

    private final String cardNumber;
}