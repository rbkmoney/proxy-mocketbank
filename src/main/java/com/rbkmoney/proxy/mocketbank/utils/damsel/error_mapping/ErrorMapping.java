package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;

import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.AuthorizationFailedEM.insufficientFunds;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.AuthorizationFailedEM.paymentToolRejected;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.AuthorizationFailedEM.unknown;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.BankCardRejectEM.*;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.FailureEM.authorizationFailed;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.FailureEM.preauthorizationError;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.PaymentToolRejectEM.bankCardReject;


/**
 * @author Anatoly Cherkasov
 */
public class ErrorMapping {

    public static Failure getFailureByCodeAndDescription(String code, String description) {
        Failure failure;

        WildCardEM wildcard = WildCardEM.findMatchWithPattern(description);
        switch (wildcard) {

            case UNSUPPORTED_CARD:
                failure = authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardUnsupported())
                        )
                );
                break;

            case THREE_D_SECURE_FAILURE:
                failure = preauthorizationError(code, description);
                break;

            case THREE_D_SECURE_TIMEOUT:
                failure = preauthorizationError(code, description);
                break;

            case INVALID_CARD:
                failure = authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardNumberInvalid())
                        )
                );
                break;

            case CVV_MATCH_FAIL:
                failure = authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cvvInvalid())
                        )
                );
                break;

            case INSUFFICIENT_FUNDS:
                failure = authorizationFailed(code, description,
                        insufficientFunds()
                );
                break;

            case EXPIRED_CARD:
                failure = authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardExpired())
                        )
                );
                break;

            case UNKNOWN:
            case UNKNOWN_FAILURE:
                failure = authorizationFailed(code, description, unknown());
                break;

            default:
                throw new WUndefinedResultException("Mocketbank: undefined error. code " + code + ", description " + description);

        }

        return failure;
    }

}
