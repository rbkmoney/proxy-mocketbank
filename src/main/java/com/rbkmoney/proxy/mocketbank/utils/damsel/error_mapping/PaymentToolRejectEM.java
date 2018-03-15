package com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping;

import com.rbkmoney.damsel.payment_processing.errors.BankCardReject;
import com.rbkmoney.damsel.payment_processing.errors.PaymentToolReject;

/**
 * @author Anatoly Cherkasov
 */
public class PaymentToolRejectEM {

    public static PaymentToolReject bankCardReject(BankCardReject bankCardReject) {
        return PaymentToolReject.bank_card_rejected(bankCardReject);
    }

}
