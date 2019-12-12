package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.proxy_provider.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Primary
@Component
public class MocketBankServerHandlerMdcDecorator implements ProviderProxySrv.Iface {

    private final MocketBankServerHandler handler;

    public MocketBankServerHandlerMdcDecorator(final MocketBankServerHandler rtn) {
        this.handler = rtn;
    }

    private void mdcPut(PaymentContext context) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        String paymentId = context.getPaymentInfo().getPayment().getId();
        MDC.put("invoiceId", invoiceId);
        MDC.put("paymentId", paymentId);
    }

    private void mdcRemove() {
        MDC.remove("invoiceId");
        MDC.remove("paymentId");
    }

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        return handler.generateToken(context);
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer callback, RecurrentTokenContext context) throws TException {
        return handler.handleRecurrentTokenCallback(callback, context);
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        mdcPut(context);
        try {
            return handler.processPayment(context);
        } finally {
            mdcRemove();
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        mdcPut(context);
        try {
            return handler.handlePaymentCallback(callback, context);
        } finally {
            mdcRemove();
        }
    }
}
