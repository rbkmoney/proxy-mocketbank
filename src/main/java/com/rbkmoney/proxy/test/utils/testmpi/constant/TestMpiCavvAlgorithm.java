package com.rbkmoney.proxy.test.utils.testmpi.constant;

/**
 * Indicates the algorithm used to generate the Cardholder Authentication Verification Value.
 */
public class TestMpiCavvAlgorithm {

    /**
     * 0: HMAC (as per SET TransStain) (no longer in use for version 1.0.2)
     */
    public final static String HMAC_AS_PER_SET_TRANS_STAIN = "0";

    /**
     * 1: CVV (no longer in use for version 1.0.2).
     */
    public final static String CVV_NO_LONGER = "1";

    /**
     * 2: CVV with ATN.
     */
    public final static String CVV_WITH_ATN = "2";

    /**
     * 3: MasterCard SPA algorithm.
     */
    public final static String MASTERCARD_SPA_ALGORITHM = "3";

}
