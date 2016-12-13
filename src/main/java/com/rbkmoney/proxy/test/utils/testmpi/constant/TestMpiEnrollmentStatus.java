package com.rbkmoney.proxy.test.utils.testmpi.constant;

/**
 * 3-D Secure enrollment status
 */
public class TestMpiEnrollmentStatus {

    /**
     * Authentication Available – Cardholder is enrolled, Activation During Shopping is
     * supported, or proof of attempted authentication available. The merchant uses the
     * URL of issuer ACS included in VERes to create the Payer Authentication Request.
     */
    public final static String AUTHENTICATION_AVAILABLE = "Y";

    /**
     * Cardholder Not Participating – Cardholder is not enrolled.
     */
    public final static String CARDHOLDER_NOT_PARTICIPATING = "N";

    /**
     * Unable to Authenticate or Card Not Eligible for Attempts
     * (such as a Commercial or anonymous Prepaid card).
     */
    public final static String UNABLE_TO_AUTHENTICATE = "U";

}
