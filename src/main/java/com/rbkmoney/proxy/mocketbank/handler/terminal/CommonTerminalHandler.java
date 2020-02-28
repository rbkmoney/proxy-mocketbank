package com.rbkmoney.proxy.mocketbank.handler.terminal;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public interface CommonTerminalHandler {

    boolean filter(final TargetInvoicePaymentStatus targetInvoicePaymentStatus, final PaymentResource paymentResource);

    PaymentProxyResult handler(PaymentContext context) throws TException;

}
