package com.rbkmoney.proxy.test.utils.testmpi.constant;


import java.util.Arrays;

public enum  TestMpiAction {

    UNKNOWN("Unknown"),
    UNSUPPORTED_CARD("Unsupported Card"),
    SUCCESS("Success"),
    THREE_D_SECURE_SUCCESS("3-D Secure Success"),
    THREE_D_SECURE_FAILURE("3-D Secure Failure"),
    THREE_D_SECURE_TIMEOUT("3-D Secure Timeout"),
    INCUFFICIENT_FUNDS("Incufficient Funds"),
    INVALID_CARD("Invalid Card"),
    CVV_MATCH_FAIL("CVV Match Fail"),
    EXPIRED_CARD("Expired Card"),
    UNKNOWN_FAILURE("Unknown Failure");

    private final String action;

    TestMpiAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static TestMpiAction findByValue(String value) {
        return Arrays.stream(values()).filter((action) -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

}