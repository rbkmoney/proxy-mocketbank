package com.rbkmoney.proxy.mocketbank.utils.qps;

import com.rbkmoney.damsel.domain.LegacyTerminalPaymentProvider;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QpsExtractors {

    public static LegacyTerminalPaymentProvider extractTerminalPaymentProvider(PaymentContext context) {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetPaymentTerminal()) {
                return paymentTool.getPaymentTerminal().getTerminalTypeDeprecated();
            }
        } else if (paymentResource.isSetRecurrentPaymentResource()) {
            PaymentTool paymentTool = paymentResource.getRecurrentPaymentResource().getPaymentTool();
            if (paymentTool.isSetPaymentTerminal()) {
                return paymentTool.getPaymentTerminal().getTerminalTypeDeprecated();
            }
        }
        return null;
    }
}
