package com.rbkmoney.proxy.mocketbank.extractor;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.proxy.mocketbank.exception.MobileException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyProviderPackageExtractors {

    public static PaymentTool extractPaymentTool(PaymentResource paymentResource) {
        if (paymentResource.isSetDisposablePaymentResource()) {
            return paymentResource.getDisposablePaymentResource().getPaymentTool();
        } else if (paymentResource.isSetRecurrentPaymentResource()) {
            return paymentResource.getRecurrentPaymentResource().getPaymentTool();
        }
        throw new MobileException("Unknown Payment Resource");
    }

    public static String extractBankCardToken(RecurrentTokenContext context) {
        return context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();
    }

    public static String extractBankCardToken(PaymentContext context) {
        return context.getPaymentInfo().getPayment().getPaymentResource().getRecurrentPaymentResource().getPaymentTool().getBankCard().getToken();
    }

}
