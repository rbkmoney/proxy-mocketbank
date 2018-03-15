package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.payment_processing.errors.AuthorizationFailure;
import com.rbkmoney.damsel.payment_processing.errors.GeneralFailure;
import com.rbkmoney.damsel.payment_processing.errors.LimitExceeded;
import com.rbkmoney.damsel.payment_processing.errors.PaymentToolReject;

/**
 * @author Anatoly Cherkasov
 */
public class AuthorizationFailedEM {

    public static AuthorizationFailure merchantBlocked() {
        return AuthorizationFailure.merchant_blocked(new GeneralFailure());
    }

    public static AuthorizationFailure operationBlocked() {
        return AuthorizationFailure.operation_blocked(new GeneralFailure());
    }

    public static AuthorizationFailure accountNotFound() {
        return AuthorizationFailure.account_not_found(new GeneralFailure());
    }

    public static AuthorizationFailure accountBlocked() {
        return AuthorizationFailure.account_blocked(new GeneralFailure());
    }

    public static AuthorizationFailure accountStolen() {
        return AuthorizationFailure.account_stolen(new GeneralFailure());
    }

    public static AuthorizationFailure accountLimitExceeded(LimitExceeded limitExceeded) {
        return AuthorizationFailure.account_limit_exceeded(limitExceeded);
    }

    public static AuthorizationFailure providerLimitExceeded(LimitExceeded limitExceeded) {
        return AuthorizationFailure.provider_limit_exceeded(limitExceeded);
    }

    public static AuthorizationFailure paymentToolRejected(PaymentToolReject paymentToolReject) {
        return AuthorizationFailure.payment_tool_rejected(paymentToolReject);
    }

    public static AuthorizationFailure insufficientFunds() {
        return AuthorizationFailure.insufficient_funds(new GeneralFailure());
    }

    public static AuthorizationFailure unknown() {
        return AuthorizationFailure.unknown(new GeneralFailure());
    }

}
