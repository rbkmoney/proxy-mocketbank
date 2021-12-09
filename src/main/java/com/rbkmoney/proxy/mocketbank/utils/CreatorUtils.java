package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatorUtils {

    public static TransactionInfo createDefaultTransactionInfo(PaymentContext context) {
        return createTransactionInfo(
                ProxyProviderPackageCreators.createInvoiceWithPayment(context.getPaymentInfo()),
                Collections.emptyMap());
    }

}
