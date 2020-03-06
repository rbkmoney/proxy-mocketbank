package com.rbkmoney.proxy.mocketbank.service.mpi.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 3-D Secure enrollment status
 */
@Getter
@RequiredArgsConstructor
public enum EnrollmentStatus {

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

    public static EnrollmentStatus valueOfByStatus(String enrollmentStatus) {
        return Arrays.stream(values())
                .filter(es -> es.getStatus().equals(enrollmentStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching constant for [" + enrollmentStatus + "]"));
    }

    public boolean isAuthenticationAvailable() {
        return this.status.equals(AUTHENTICATION_AVAILABLE.getStatus());
    }

}
