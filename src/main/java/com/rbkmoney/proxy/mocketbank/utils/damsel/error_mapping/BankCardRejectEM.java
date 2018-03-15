package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.payment_processing.errors.BankCardReject;
import com.rbkmoney.damsel.payment_processing.errors.GeneralFailure;

/**
 * @author Anatoly Cherkasov
 */
public class BankCardRejectEM {

    public static BankCardReject cardExpired() {
        return BankCardReject.card_expired(new GeneralFailure());
    }

    public static BankCardReject cardNumberInvalid() {
        return BankCardReject.card_number_invalid(new GeneralFailure());
    }

    public static BankCardReject cardHolderInvalid() {
        return BankCardReject.card_holder_invalid(new GeneralFailure());
    }

    public static BankCardReject cvvInvalid() {
        return BankCardReject.cvv_invalid(new GeneralFailure());
    }

    public static BankCardReject cardUnsupported() {
        return BankCardReject.card_unsupported(new GeneralFailure());
    }

    public static BankCardReject issuerNotFound() {
        return BankCardReject.issuer_not_found(new GeneralFailure());
    }

    // TODO: wait structure
    public static BankCardReject restictedCard() {
        return BankCardReject.card_expired(new GeneralFailure());
    }

}
