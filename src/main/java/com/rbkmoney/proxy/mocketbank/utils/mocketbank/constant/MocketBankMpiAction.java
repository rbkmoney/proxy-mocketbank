package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MocketBankMpiAction {

    UNKNOWN("Unknown"),
    UNSUPPORTED_CARD("Unsupported Card"),
    SUCCESS("Success"),
    THREE_D_SECURE_SUCCESS("3-D Secure Success"),
    THREE_D_SECURE_FAILURE("3-D Secure Failure"),
    THREE_D_SECURE_TIMEOUT("3-D Secure Timeout"),
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    INVALID_CARD("Invalid Card"),
    CVV_MATCH_FAIL("CVV Match Fail"),
    EXPIRED_CARD("Expired Card"),
    APPLE_PAY_FAILURE("Apple Pay Failure"),
    APPLE_PAY_SUCCESS("Apple Pay Success"),
    GOOGLE_PAY_FAILURE("Google Pay Failure"),
    GOOGLE_PAY_SUCCESS("Google Pay Success"),
    SAMSUNG_PAY_FAILURE("Samsung Pay Failure"),
    SAMSUNG_PAY_SUCCESS("Samsung Pay Success"),
    UNKNOWN_FAILURE("Unknown Failure");

    private final String action;

    public static MocketBankMpiAction findByValue(String value) {
        return Arrays.stream(values()).filter((action) -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

}