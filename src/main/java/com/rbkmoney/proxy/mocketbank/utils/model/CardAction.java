package com.rbkmoney.proxy.mocketbank.utils.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CardAction {

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

    public static CardAction findByValue(String value) {
        return Arrays.stream(values()).filter((action) -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static boolean isCardEnrolled(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardEnrolled(action);
    }

    public static boolean isCardEnrolled(CardAction action) {
        return Arrays.asList(enrolledCard()).contains(action);
    }

    private static CardAction[] enrolledCard() {
        return new CardAction[]{
                THREE_D_SECURE_FAILURE,
                THREE_D_SECURE_TIMEOUT,
                THREE_D_SECURE_SUCCESS
        };
    }

    public static boolean isCardFailed(CardAction action) {
        return Arrays.asList(failedCard()).contains(action);
    }

    private static CardAction[] failedCard() {
        return new CardAction[]{
                INSUFFICIENT_FUNDS,
                INVALID_CARD,
                CVV_MATCH_FAIL,
                APPLE_PAY_FAILURE,
                SAMSUNG_PAY_FAILURE,
                GOOGLE_PAY_FAILURE,
                EXPIRED_CARD,
                UNKNOWN_FAILURE
        };
    }

    public static boolean isCardSuccess(CardAction action) {
        return Arrays.asList(successCard()).contains(action);
    }

    private static CardAction[] successCard() {
        return new CardAction[]{
                SUCCESS,
                APPLE_PAY_SUCCESS,
                GOOGLE_PAY_SUCCESS,
                SAMSUNG_PAY_SUCCESS
        };
    }

    public static boolean isMpiCardFailed(CardAction action) {
        return Arrays.asList(mpiFailedCard()).contains(action);
    }

    private static CardAction[] mpiFailedCard() {
        return new CardAction[]{
                THREE_D_SECURE_FAILURE,
                THREE_D_SECURE_TIMEOUT,
                UNKNOWN_FAILURE
        };
    }

}