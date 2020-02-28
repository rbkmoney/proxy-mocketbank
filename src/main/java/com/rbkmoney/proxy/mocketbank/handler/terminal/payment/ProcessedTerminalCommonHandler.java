package com.rbkmoney.proxy.mocketbank.handler.terminal.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.proxy.mocketbank.handler.terminal.CommonTerminalHandler;
import com.rbkmoney.proxy.mocketbank.utils.PaymentUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;
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
        TransactionInfo transactionInfo = createTransactionInfo(
                PaymentUtils.generateTransactionId(context.getPaymentInfo()), Collections.emptyMap()
        );
        return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getState().getBytes(), transactionInfo);
    }

}
