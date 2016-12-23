package com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant;

/**
 * Transaction Status
 */
public class MocketBankMpiTransactionStatus {

    /**
     * The merchant submits an authorization request including the
     * ECI and CAVV supplied in the PARes.
     */
    public final static String AUTHENTICATION_SUCCESSFUL = "Y";

    /**
     * The merchant must not submit a failed authentication for
     * authorization.
     */
    public final static String AUTHENTICATION_FAILED = "N";

    /**
     * The merchant may process an authorization request using the
     * appropriate ECI.
     */
    public final static String AUTHENTICATION_COULD_NOT_BE_PERFORMED = "U";

    /**
     * The merchant submits an authorization request including the
     * ECI and CAVV supplied in the PARes.
     */
    public final static String ATTEMPTS_PROCESSING_PERFORMED = "A";

}
