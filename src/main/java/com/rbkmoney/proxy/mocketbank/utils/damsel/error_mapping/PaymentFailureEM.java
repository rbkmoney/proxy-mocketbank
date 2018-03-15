package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.payment_processing.errors.AuthorizationFailure;
import com.rbkmoney.damsel.payment_processing.errors.GeneralFailure;
import com.rbkmoney.damsel.payment_processing.errors.PaymentFailure;

/**
 * @author Anatoly Cherkasov
 */
public class PaymentFailureEM {

    public static PaymentFailure preauthorizationFailed() {
        return PaymentFailure.preauthorization_failed(new GeneralFailure());
    }

    public static PaymentFailure authorizationFailed(AuthorizationFailure authorizationFailure) {
        return PaymentFailure.authorization_failed(authorizationFailure);
    }

    public static PaymentFailure rejectedByInspector() {
        return PaymentFailure.rejected_by_inspector(new GeneralFailure());
    }

}
