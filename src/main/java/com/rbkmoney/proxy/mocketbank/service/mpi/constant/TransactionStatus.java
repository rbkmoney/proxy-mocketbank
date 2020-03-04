package com.rbkmoney.proxy.mocketbank.service.mpi.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {

    /**
     * The merchant submits an authorization request including the
     * ECI and CAVV supplied in the PARes.
     */
    AUTHENTICATION_SUCCESSFUL("Y"),

    /**
     * The merchant must not submit a failed authentication for
     * authorization.
     */
    AUTHENTICATION_FAILED("N"),

    /**
     * The merchant may process an authorization request using the
     * appropriate ECI.
     */
    AUTHENTICATION_COULD_NOT_BE_PERFORMED("U"),

    /**
     * The merchant submits an authorization request including the
     * ECI and CAVV supplied in the PARes.
     */
    ATTEMPTS_PROCESSING_PERFORMED("A");

    private final String status;

    public static boolean isAuthenticationSuccessful(String status) {
        return AUTHENTICATION_SUCCESSFUL.getStatus().equalsIgnoreCase(status);
    }

}
