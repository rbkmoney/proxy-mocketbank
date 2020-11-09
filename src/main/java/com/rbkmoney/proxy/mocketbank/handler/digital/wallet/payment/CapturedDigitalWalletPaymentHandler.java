package com.rbkmoney.proxy.mocketbank.handler.digital.wallet.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.Intent;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import com.rbkmoney.proxy.mocketbank.validator.DigitalWalletCapturedValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapturedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final DigitalWalletCapturedValidator digitalWalletCapturedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetCaptured();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        digitalWalletCapturedValidator.validate(context, context.getOptions());
        Intent intent = createFinishIntentSuccess();
        TransactionInfo transactionInfo = extractTransactionInfo(context);
        return createPaymentProxyResult(intent, extractSessionState(context), transactionInfo);
    }

}
