package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MpiAction {

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

    public static MpiAction findByValue(String value) {
        return Arrays.stream(values()).filter((action) -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static boolean isCardEnrolled(Card card) {
        MpiAction action = MpiAction.findByValue(card.getAction());
        return MpiAction.isCardEnrolled(action);
    }

    public static boolean isCardEnrolled(MpiAction action) {
        return Arrays.asList(enrolledCard()).contains(action);
    }

    private static MpiAction[] enrolledCard() {
        return new MpiAction[]{
                THREE_D_SECURE_FAILURE,
                THREE_D_SECURE_TIMEOUT,
                THREE_D_SECURE_SUCCESS
        };
    }

}