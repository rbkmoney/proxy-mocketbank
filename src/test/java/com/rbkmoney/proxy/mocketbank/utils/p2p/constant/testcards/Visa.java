package com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Visa implements TestCard {
    SUCCESS("4242424242424242"),
    SUCCESS_3DS("4012888888881881"),
    FAILURE_3DS("4987654321098769"),
    TIMEOUT_3DS("4342561111111118"),
    INSUFFICIENT_FUNDS("4000000000000002"),
    INVALID_CARD("4222222222222220"),
    CVV_MATCH_FAIL("4003830171874018"),
    EXPIRED("4000000000000069"),
    UNKNOWN_FAILURE("4111110000000112"),
    APPLE_PAY_FAILURE("5000000000000009"),
    APPLE_PAY_SUCCESS("4300000000000777");

    private final String cardNumber;
}
