package com.rbkmoney.proxy.mocketbank.extractor;

import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.exception.MobileException;

public class ProxyProviderPackageExtractorsExtends extends ProxyProviderPackageExtractors {

    public static PaymentTool extractPaymentTool(PaymentResource paymentResource) {
        if (paymentResource.isSetDisposablePaymentResource()) {
            return paymentResource.getDisposablePaymentResource().getPaymentTool();
        } else if (paymentResource.isSetRecurrentPaymentResource()) {
            return paymentResource.getRecurrentPaymentResource().getPaymentTool();
        }
        throw new MobileException("Unknown Payment Resource");
    }

}
