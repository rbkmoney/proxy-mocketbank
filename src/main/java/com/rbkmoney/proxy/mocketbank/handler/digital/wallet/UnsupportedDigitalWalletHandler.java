package com.rbkmoney.proxy.mocketbank.handler.digital.wallet;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public class UnsupportedDigitalWalletHandler implements CommonDigitalWalletHandler {

    @Override
    public boolean filter(
            final TargetInvoicePaymentStatus targetInvoicePaymentStatus,
            final PaymentResource paymentResource) {
        return false;
    }

    @Override
    public PaymentProxyResult handler(final PaymentContext context) throws TException {
        throw new TException("Unsupported method");
    }

}
