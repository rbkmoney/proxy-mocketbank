package com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Maestro implements TestCard {
    SUCCESS("586824160825533338");

    private final String cardNumber;
}