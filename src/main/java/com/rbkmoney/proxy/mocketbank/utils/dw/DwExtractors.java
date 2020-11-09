package com.rbkmoney.proxy.mocketbank.utils.dw;

import com.rbkmoney.damsel.domain.DigitalWallet;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DwExtractors {

    public static DigitalWallet extractDigitalWallet(PaymentContext context) {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetDigitalWallet()) {
                return paymentTool.getDigitalWallet();
            }
        }
        return null;
    }
}
