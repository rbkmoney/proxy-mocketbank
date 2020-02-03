package com.rbkmoney.proxy.mocketbank.utils.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreditCardUtils {

    private static final String MASK_CHAR = "*";

    public static String maskNumber(final String creditCardNumber, int startLength, int endLength, String maskChar) {
        final String cardNumber = creditCardNumber.replaceAll("\\D", "");

        final int end = cardNumber.length() - endLength;
        final String overlay = StringUtils.repeat(maskChar, end - startLength);

        return StringUtils.overlay(cardNumber, overlay, startLength, end);
    }

    public static String maskNumber(final String creditCardNumber) {
        return maskNumber(creditCardNumber, 4, 4, MASK_CHAR);
    }

}
