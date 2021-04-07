package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TransactionStatusReason {

    CARD_AUTHENTICATION_FAILED("01", "Card authentication failed"),
    UNKNOWN_DEVICE("02", "Unknown Device"),
    UNSUPPORTED_DEVICE("03", "Unsupported Device"),
    EXCEEDS_AUTHENTICATION_FREQUENCY_LIMIT("04", "Exceeds authentication frequency limit"),
    EXPIRED_CARD("05", "Expired card"),
    INVALID_CARD_NUMBER("06", "Invalid card number"),
    INVALID_TRANSACTION("07", "Invalid transaction"),
    NO_CARD_RECORD("08", "No Card record"),
    SECURITY_FAILURE("09", "Security failure"),
    STOLEN_CARD("10", "Stolen card"),
    SUSPECTED_FRAUD("11", "Suspected fraud"),
    TRANSACTION_NOT_PERMITTED_TO_CARDHOLDER("12", "Transaction not permitted to cardholder"),
    CARDHOLDER_NOT_ENROLLED_IN_SERVICE("13", "Cardholder not enrolled in service"),
    TRANSACTION_TIMED_OUT_AT_THE_ACS("14", "Transaction timed out at the ACS"),
    LOW_CONFIDENCE("15", "Low confidence"),
    MEDIUM_CONFIDENCE("16", "Medium confidence"),
    HIGH_CONFIDENCE("17", "High confidence"),
    VERY_HIGH_CONFIDENCE("18", "Very High confidence"),
    EXCEEDS_ACS_MAXIMUM_CHALLENGES("19", "Exceeds ACS maximum challenges"),
    NON_PAYMENT_TRANSACTION_NOT_SUPPORTED("20", "Non-Payment transaction not supported"),
    THREERI_TRANSACTION_NOT_SUPPORTED("21", "3RI transaction not supported"),
    RESERVED_FOR_EMVCO_FUTURE_USE("22-79", "Reserved for EMVCo future use (values invalid until defined by EMVCo)"),
    RESERVED_FOR_DS_USE("80-99", "Reserved for DS use");

    private final String code;
    private final String reason;

    public static TransactionStatusReason getByCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
