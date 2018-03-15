package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.payment_processing.errors.GeneralFailure;
import com.rbkmoney.damsel.payment_processing.errors.LimitExceeded;

/**
 * @author Anatoly Cherkasov
 */
public class LimitExceededEM {

    public static LimitExceeded amount() {
        return LimitExceeded.amount(new GeneralFailure());
    }

    public static LimitExceeded number() {
        return LimitExceeded.number(new GeneralFailure());
    }

    public static LimitExceeded unknown() {
        return LimitExceeded.unknown(new GeneralFailure());
    }

}
