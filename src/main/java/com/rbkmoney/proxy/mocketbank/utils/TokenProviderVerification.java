package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.domain.LegacyBankCardTokenProvider;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.*;

public class TokenProviderVerification {

    public static boolean hasBankCardTokenProvider(Object object) {
        LegacyBankCardTokenProvider bankCardTokenProvider;
        if (object instanceof RecurrentTokenContext) {
            bankCardTokenProvider = extractBankCardTokenProvider((RecurrentTokenContext)object);
        } else {
            bankCardTokenProvider = extractBankCardTokenProvider((PaymentContext)object);
        }

        return bankCardTokenProvider != null;
    }


    public static LegacyBankCardTokenProvider extractBankCardTokenProvider(PaymentContext context) {
        PaymentResource paymentResource = context.getPaymentInfo().getPayment().getPaymentResource();
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetBankCard() && paymentTool.getBankCard().isSetTokenProviderDeprecated()) {
                return paymentTool.getBankCard().getTokenProviderDeprecated();
            }
        }

        return null;
    }

    public static LegacyBankCardTokenProvider extractBankCardTokenProvider(RecurrentTokenContext context) {
        PaymentTool paymentTool = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool();
        return paymentTool.isSetBankCard() && paymentTool.getBankCard().isSetTokenProviderDeprecated()
                ? paymentTool.getBankCard().getTokenProviderDeprecated()
                : null;
    }
}
