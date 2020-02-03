package com.rbkmoney.proxy.mocketbank.utils.mobilephone;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MobilePhoneAction {

    UNKNOWN("Unknown"),
    SUCCESS("Success"),
    UNSUPPORTED_PHONE("Unsupported phone"),
    INSUFFICIENT_FUNDS("Insufficient Funds");

    private final String action;

    public static MobilePhoneAction findByValue(String value) {
        return Arrays.stream(values())
                .filter(action -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static MobilePhoneAction[] failedActions() {
        return new MobilePhoneAction[]{
                UNKNOWN, UNSUPPORTED_PHONE, INSUFFICIENT_FUNDS
        };
    }

    public static boolean hasStatus(MobilePhoneAction[] actions, String value) {
        return Arrays.stream(actions).anyMatch(action -> action.getAction().equalsIgnoreCase(value));
    }

    public static boolean isFailedAction(String action) {
        return hasStatus(failedActions(), action);
    }

}