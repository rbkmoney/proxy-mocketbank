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

        WildCardEM wildcard = WildCardEM.findMatchWithPattern(description);

        switch (wildcard) {

            case UNSUPPORTED_CARD:
                return authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardUnsupported())
                        )
                );

            case THREE_D_SECURE_FAILURE:
                return preauthorizationError(code, description);

            case THREE_D_SECURE_TIMEOUT:
                return preauthorizationError(code, description);

            case INVALID_CARD:
                return authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardNumberInvalid())
                        )
                );

            case CVV_MATCH_FAIL:
                return authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cvvInvalid())
                        )
                );

            case INSUFFICIENT_FUNDS:
                return authorizationFailed(code, description,
                        insufficientFunds()
                );

            case EXPIRED_CARD:
                return authorizationFailed(code, description,
                        paymentToolRejected(
                                bankCardReject(cardExpired())
                        )
                );

            case UNKNOWN:
            case UNKNOWN_FAILURE:
                return authorizationFailed(code, description, unknown());

            default:
                throw new WUndefinedResultException("Mocketbank: undefined error. code " + code + ", description " + description);

        }

    }

}
