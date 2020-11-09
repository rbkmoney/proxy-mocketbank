package com.rbkmoney.proxy.mocketbank.handler.digital.wallet.payment;

import com.rbkmoney.damsel.domain.DigitalWallet;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import com.rbkmoney.proxy.mocketbank.utils.dw.DwCreators;
import com.rbkmoney.proxy.mocketbank.utils.dw.DwExtractors;
import com.rbkmoney.proxy.mocketbank.utils.dw.DwOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractCashPayment;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractInvoiceId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetDisposablePaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        String invoiceId = extractInvoiceId(context);
        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);

        if (Arrays.equals(context.getSession().getState(), PaymentState.SLEEP.getBytes())) {
            return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo);
        }

        Cash cash = extractCashPayment(context);
        DigitalWallet digitalWallet = DwExtractors.extractDigitalWallet(context);
        HashMap<String, String> params = DwCreators.createDWParams(invoiceId, cash, digitalWallet, mockBankProperties);
        String url = UrlUtils.getCallbackUrl(mockBankProperties.getCallbackUrl(), mockBankProperties.getPathDWCallbackUrl());

        return createPaymentProxyResult(
                createIntentWithSleepIntent(
                        DwOptions.extractDWTimerTimeout(context.getOptions(), timerProperties.getDwTimeout()),
                        createPostUserInteraction(url, params)
                ),
                PaymentState.SLEEP.getBytes(), transactionInfo);
    }
}
