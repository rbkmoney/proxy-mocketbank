package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 3-D Secure enrollment status
 */
@Getter
@RequiredArgsConstructor
public enum MpiEnrollmentStatus {

    /**
     * Authentication Available – Cardholder is enrolled, Activation During Shopping is
     * supported, or proof of attempted authentication available. The merchant uses the
     * URL of issuer ACS included in VERes to create the Payer Authentication Request.
     */
    AUTHENTICATION_AVAILABLE("Y"),

    /**
     * Cardholder Not Participating – Cardholder is not enrolled.
     */
    CARDHOLDER_NOT_PARTICIPATING("N"),

    /**
     * Unable to Authenticate or Card Not Eligible for Attempts
     * (such as a Commercial or anonymous Prepaid card).
     */
    UNABLE_TO_AUTHENTICATE("U");

    private final String status;

}
