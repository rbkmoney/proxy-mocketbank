package com.rbkmoney.proxy.mocketbank.handler.digital.wallet.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import com.rbkmoney.proxy.mocketbank.validator.DigitalWalletRefundedValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createFinishIntentSuccess;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPaymentProxyResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final DigitalWalletRefundedValidator digitalWalletRefundedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetRefunded();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        digitalWalletRefundedValidator.validate(context, context.getOptions());
        return createPaymentProxyResult(createFinishIntentSuccess(),
                PaymentState.REFUNDED.getBytes(), context.getPaymentInfo().getRefund().getTrx()
        );
    }

}
