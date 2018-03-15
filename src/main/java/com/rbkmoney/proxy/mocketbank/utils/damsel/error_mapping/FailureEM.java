package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.payment_processing.errors.AuthorizationFailure;
import com.rbkmoney.damsel.payment_processing.errors.PaymentFailure;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;

/**
 * @author Anatoly Cherkasov
 */
public class FailureEM {

    public static final String REJECTED_BY_INSPECTOR = "rejected_by_inspector";
    public static final String PRE_AUTHORIZATION_FAILED = "preauthorization_failed";
    public static final String AUTHORIZATION_FAILED = "authorization_failed";


    public static Failure rejectedByInspector(String code, String description) {
        return failure(PaymentFailureEM.rejectedByInspector(),
                REJECTED_BY_INSPECTOR, code, description);
    }

    public static Failure preauthorizationError(String code, String description) {
        return failure(PaymentFailureEM.preauthorizationFailed(),
                PRE_AUTHORIZATION_FAILED, code, description);
    }

    public static Failure authorizationFailed(String code, String description, AuthorizationFailure authorizationFailure) {
        return failure(PaymentFailureEM.authorizationFailed(authorizationFailure),
                AUTHORIZATION_FAILED, code, description);
    }

    private static Failure failure(PaymentFailure paymentFailure, String typeFailure, String code, String description) {
        Failure failure = toGeneral(paymentFailure);
        failure.setCode(typeFailure);
        failure.setReason(
                prepareReason(code, description)
        );
        return failure;
    }

    private static String prepareReason(String code, String description) {
        return String.format("'%s' - '%s'", code, description);
    }

}
