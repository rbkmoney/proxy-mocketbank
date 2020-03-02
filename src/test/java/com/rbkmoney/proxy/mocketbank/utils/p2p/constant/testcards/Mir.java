package com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mir implements TestCard {
    SUCCESS("2201382000000013");

    private final String cardNumber;
}
