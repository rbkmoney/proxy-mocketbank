package com.rbkmoney.proxy.mocketbank.handler.payment.common;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import com.rbkmoney.proxy.mocketbank.validator.PaymentCapturedValidator;
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
public class CapturedCommonPaymentHandler implements CommonPaymentHandler {

    private final PaymentCapturedValidator paymentCapturedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return (targetInvoicePaymentStatus.isSetCaptured());
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        paymentCapturedValidator.validate(context, context.getOptions());
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }
        InvoicePayment payment = context.getPaymentInfo().getPayment();
        return createPaymentProxyResult(intent, PaymentState.CONFIRM.getBytes(), payment.getTrx());
    }

}
