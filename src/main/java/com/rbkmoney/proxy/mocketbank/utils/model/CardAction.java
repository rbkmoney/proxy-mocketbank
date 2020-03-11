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

    private static final CardAction[] SUCCESS_CARDS_APPLE_PAY = {
            APPLE_PAY_SUCCESS,
    };

    private static final CardAction[] SUCCESS_CARDS_GOOGLE_PAY = {
            GOOGLE_PAY_SUCCESS,
    };

    private static final CardAction[] SUCCESS_CARDS_SAMSUNG_PAY = {
            SAMSUNG_PAY_SUCCESS
    };

    private static final CardAction[] MPI_FAILED_CARDS = {
            FAILURE_3DS,
            TIMEOUT_3DS
    };

    private static final CardAction[] MPI_TIMEOUT_CARDS = {
            TIMEOUT_3DS
    };

    private static final CardAction[] MPI_SUCCESS_CARDS = {
            SUCCESS_3DS
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

    public static boolean isCardFailed(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardFailed(action);
    }

    public static boolean isCardFailed(CardAction action) {
        return Arrays.asList(FAILED_CARDS).contains(action);
    }

    public static boolean isCardSuccess(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardSuccess(action);
    }

    public static boolean isCardSuccess(CardAction action) {
        return Arrays.asList(SUCCESS_CARDS).contains(action);
    }

    public static boolean isCardSuccessApplePay(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardSuccess(action);
    }

    public static boolean isCardSuccessApplePay(CardAction action) {
        return Arrays.asList(SUCCESS_CARDS_APPLE_PAY).contains(action);
    }

    public static boolean isCardSuccessSamsungPay(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardSuccess(action);
    }

    public static boolean isCardSuccessSamsungPay(CardAction action) {
        return Arrays.asList(SUCCESS_CARDS_SAMSUNG_PAY).contains(action);
    }

    public static boolean isCardSuccessGooglePay(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return CardAction.isCardSuccess(action);
    }

    public static boolean isCardSuccessGooglePay(CardAction action) {
        return Arrays.asList(SUCCESS_CARDS_GOOGLE_PAY).contains(action);
    }

    public static boolean isMpiCardFailed(CardAction action) {
        return Arrays.asList(MPI_FAILED_CARDS).contains(action);
    }

    public static boolean isMpiCardFailed(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return isMpiCardFailed(action);
    }

    public static boolean isMpiCardSuccess(CardAction action) {
        return Arrays.asList(MPI_SUCCESS_CARDS).contains(action);
    }

    public static boolean isMpiCardSuccess(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return isMpiCardSuccess(action);
    }

    public static boolean isMpiCardTimeout(CardAction action) {
        return Arrays.asList(MPI_SUCCESS_CARDS).contains(action);
    }

    public static boolean isMpiCardTimeout(Card card) {
        CardAction action = CardAction.findByValue(card.getAction());
        return isMpiCardTimeout(action);
    }

}