package com.rbkmoney.proxy.mocketbank.handler.payment.recurrent;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.Intent;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractInvoiceId;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isMakeRecurrent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedRecurrentCommonPaymentHandler implements CommonPaymentHandler {

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetRecurrentPaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
        return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
    }
}
