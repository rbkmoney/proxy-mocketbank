package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;


import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction;

import java.util.Arrays;

/**
 * @author Anatoly Cherkasov
 */
public enum WildCardEM {

    UNDEFINED("Undefined", "Undefined"),
    UNKNOWN(MocketBankMpiAction.UNKNOWN.getAction(), "Unknown"),
    UNSUPPORTED_CARD(MocketBankMpiAction.UNSUPPORTED_CARD.getAction(), "Unsupported Card"),
    THREE_D_SECURE_FAILURE(MocketBankMpiAction.THREE_D_SECURE_FAILURE.getAction(), "3-D Secure Failure"),
    THREE_D_SECURE_TIMEOUT(MocketBankMpiAction.THREE_D_SECURE_TIMEOUT.getAction(), "3-D Secure Timeout"),
    INSUFFICIENT_FUNDS(MocketBankMpiAction.INSUFFICIENT_FUNDS.getAction(), "Insufficient Funds"),
    INVALID_CARD(MocketBankMpiAction.INVALID_CARD.getAction(), "Invalid Card"),
    CVV_MATCH_FAIL(MocketBankMpiAction.CVV_MATCH_FAIL.getAction(), "CVV Match Fail"),
    EXPIRED_CARD(MocketBankMpiAction.EXPIRED_CARD.getAction(), "Expired Card"),
    UNKNOWN_FAILURE(MocketBankMpiAction.UNKNOWN_FAILURE.getAction(), "Unknown Failure");

    private final String error;
    private final String pattern;

    WildCardEM(String error, String pattern) {
        this.error = error;
        this.pattern = pattern;
    }

    public String getError() {
        return error;
    }

    public String getPattern() {
        return pattern;
    }

    public static WildCardEM findMatchWithPattern(String value) {
        return Arrays.stream(values())
                .filter((wildcard) -> value.matches(wildcard.getPattern()))
                .findFirst()
                .orElse(UNDEFINED);
    }

}
