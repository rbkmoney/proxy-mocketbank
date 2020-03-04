package com.rbkmoney.proxy.mocketbank.service.mpi.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Indicates the algorithm used to generate
 * the Cardholder Authentication Verification Value
 */
@Getter
@RequiredArgsConstructor
public enum CavvAlgorithm {

    /**
     * 0: HMAC (as per SET TransStain) (no longer in use for version 1.0.2)
     */
    HMAC_AS_PER_SET_TRANS_STAIN("0"),

    /**
     * 1: CVV (no longer in use for version 1.0.2).
     */
    CVV_NO_LONGER("1"),

    /**
     * 2: CVV with ATN.
     */
    CVV_WITH_ATN("2"),

    /**
     * 3: MasterCard SPA algorithm.
     */
    MASTERCARD_SPA_ALGORITHM("3");

    private final String algorithm;
}
