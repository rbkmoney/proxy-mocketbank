package com.rbkmoney.proxy.mocketbank.utils.creator;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyProviderCreator {

    public static final String DELIMITER = ".";

    public static TransactionInfo createDefaultTransactionInfo(PaymentContext context) {
        return createTransactionInfo(createTransactionId(context.getPaymentInfo()), Collections.emptyMap());
    }

    public static String createTransactionId(PaymentInfo payment) {
        return payment.getInvoice().getId() + DELIMITER + payment.getPayment().getId();
    }
}
