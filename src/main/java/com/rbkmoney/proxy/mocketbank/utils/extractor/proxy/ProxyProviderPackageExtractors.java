package com.rbkmoney.proxy.mocketbank.utils.extractor.proxy;

import com.rbkmoney.damsel.domain.BankCardTokenProvider;
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

    public static BankCardTokenProvider extractBankCardTokenProvider(PaymentContext context) {
        PaymentResource paymentResource = context.getPaymentInfo().getPayment().getPaymentResource();
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetBankCard() && paymentTool.getBankCard().isSetTokenProvider()) {
                return paymentTool.getBankCard().getTokenProvider();
            }
        }
        return null;
    }

    public static BankCardTokenProvider extractBankCardTokenProvider(RecurrentTokenContext context) {
        PaymentTool paymentTool = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool();
        if (paymentTool.isSetBankCard() && paymentTool.getBankCard().isSetTokenProvider()) {
            return paymentTool.getBankCard().getTokenProvider();
        }
        return null;
    }

    public static boolean hasBankCardTokenProvider(Object object) {
        BankCardTokenProvider bankCardTokenProvider;
        if (object instanceof RecurrentTokenContext) {
            bankCardTokenProvider = extractBankCardTokenProvider((RecurrentTokenContext) object);
        } else {
            bankCardTokenProvider = extractBankCardTokenProvider((PaymentContext) object);
        }
        return (bankCardTokenProvider != null);
    }
}
