package com.rbkmoney.proxy.mocketbank.handler.terminal.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.handler.terminal.CommonTerminalHandler;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createFinishIntentSuccess;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPaymentProxyResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedTerminalCommonHandler implements CommonTerminalHandler {

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        TransactionInfo transactionInfo =  CreatorUtils.createDefaultTransactionInfo(context);
        return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo);
    }

}
