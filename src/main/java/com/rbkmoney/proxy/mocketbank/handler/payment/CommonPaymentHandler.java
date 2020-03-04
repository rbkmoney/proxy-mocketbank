package com.rbkmoney.proxy.mocketbank.handler.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public interface CommonPaymentHandler {

    boolean filter(final TargetInvoicePaymentStatus targetInvoicePaymentStatus, final PaymentResource paymentResource);

    PaymentProxyResult handler(PaymentContext context) throws TException;

}
