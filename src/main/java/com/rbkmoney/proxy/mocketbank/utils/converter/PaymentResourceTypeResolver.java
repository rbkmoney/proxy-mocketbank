package com.rbkmoney.proxy.mocketbank.utils.converter;

import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResourceTypeResolver {

    public static String extractPaymentResourceType(PaymentContext paymentContext) {
        if (paymentContext == null) {
            throw new IllegalArgumentException("PaymentContext cannot be empty");
        } else if (paymentContext.getSession() == null) {
            throw new IllegalArgumentException("Payment context session cannot be empty");
        }
        return extractPaymentResourceType(paymentContext.getPaymentInfo().getPayment().getPaymentResource());
    }

    private static String extractPaymentResourceType(PaymentResource paymentResource) {
        return (paymentResource.isSetRecurrentPaymentResource())
                ? PaymentResourceType.RECURRENT.name()
                : PaymentResourceType.PAYMENT.name();
    }

}
