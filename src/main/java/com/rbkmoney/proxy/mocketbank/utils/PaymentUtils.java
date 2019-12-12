package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.proxy_provider.PaymentInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentUtils {

    public static String generateTransactionId(PaymentInfo payment) {
        return payment.getInvoice().getId() + payment.getPayment().getId();
    }

}
