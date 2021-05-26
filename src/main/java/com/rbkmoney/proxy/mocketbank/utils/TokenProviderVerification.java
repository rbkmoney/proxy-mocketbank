package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.*;

import java.util.Optional;

public class TokenProviderVerification {

    public static boolean hasBankCardTokenProvider(Object object) {
        LegacyBankCardTokenProvider bankCardTokenProvider;
        if (object instanceof RecurrentTokenContext) {
            bankCardTokenProvider = extractBankCardTokenProvider((RecurrentTokenContext) object);
        } else {
            bankCardTokenProvider = extractBankCardTokenProvider((PaymentContext) object);
        }

        return bankCardTokenProvider != null;
    }


    public static LegacyBankCardTokenProvider extractBankCardTokenProvider(PaymentContext context) {
        return Optional.ofNullable(context.getPaymentInfo())
                .map(PaymentInfo::getPayment)
                .map(InvoicePayment::getPaymentResource)
                .map(PaymentResource::getDisposablePaymentResource)
                .map(DisposablePaymentResource::getPaymentTool)
                .map(PaymentTool::getBankCard)
                .map(BankCard::getTokenProviderDeprecated)
                .orElse(null);
    }

    public static LegacyBankCardTokenProvider extractBankCardTokenProvider(RecurrentTokenContext context) {
        return Optional.ofNullable(context.getTokenInfo())
                .map(RecurrentTokenInfo::getPaymentTool)
                .map(RecurrentPaymentTool::getPaymentResource)
                .map(DisposablePaymentResource::getPaymentTool)
                .map(PaymentTool::getBankCard)
                .map(BankCard::getTokenProviderDeprecated)
                .orElse(null);
    }
}
