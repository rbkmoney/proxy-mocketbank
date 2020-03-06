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
    SUCCESS_3DS("3-D Secure Success"),
    FAILURE_3DS("3-D Secure Failure"),
    TIMEOUT_3DS("3-D Secure Timeout"),
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

    private static final CardAction[] ENROLLED_CARDS = {
            FAILURE_3DS,
            TIMEOUT_3DS,
            SUCCESS_3DS
    };

    private static final CardAction[] FAILED_CARDS = {
            INSUFFICIENT_FUNDS,
            INVALID_CARD,
            CVV_MATCH_FAIL,
            APPLE_PAY_FAILURE,
            SAMSUNG_PAY_FAILURE,
            GOOGLE_PAY_FAILURE,
            EXPIRED_CARD,
            UNKNOWN_FAILURE
    };

    private static final CardAction[] SUCCESS_CARDS = {
            SUCCESS,
            APPLE_PAY_SUCCESS,
            GOOGLE_PAY_SUCCESS,
            SAMSUNG_PAY_SUCCESS
    };

    private static final CardAction[] MPI_FAILED_CARDS = {
            FAILURE_3DS,
            TIMEOUT_3DS,
            UNKNOWN_FAILURE
    };

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
        return Arrays.asList(ENROLLED_CARDS).contains(action);
    }

    public static boolean isCardFailed(CardAction action) {
        return Arrays.asList(FAILED_CARDS).contains(action);
    }

    public static boolean isCardSuccess(CardAction action) {
        return Arrays.asList(SUCCESS_CARDS).contains(action);
    }

    public static boolean isMpiCardFailed(CardAction action) {
        return Arrays.asList(MPI_FAILED_CARDS).contains(action);
    }

}