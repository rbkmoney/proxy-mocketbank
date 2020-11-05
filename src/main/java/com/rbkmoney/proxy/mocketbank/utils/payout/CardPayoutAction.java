package com.rbkmoney.proxy.mocketbank.utils.payout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CardPayoutAction {

    UNKNOWN("Unknown"),
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    INVALID_CARD("Invalid Card"),
    EXPIRED_CARD("Expired Card"),
    UNKNOWN_FAILURE("Unknown Failure");

    private static final CardPayoutAction[] FAILED_CARDS = {
            INSUFFICIENT_FUNDS,
            INVALID_CARD,
            EXPIRED_CARD,
            UNKNOWN_FAILURE
    };

    private final String action;

    public static CardPayoutAction findByValue(String value) {
        return Arrays.stream(values()).filter(action -> action.getAction().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static boolean isCardFailed(CardPayout card) {
        CardPayoutAction action = CardPayoutAction.findByValue(card.getAction());
        return CardPayoutAction.isCardFailed(action);
    }

    public static boolean isCardFailed(CardPayoutAction action) {
        return Arrays.asList(FAILED_CARDS).contains(action);
    }

}
