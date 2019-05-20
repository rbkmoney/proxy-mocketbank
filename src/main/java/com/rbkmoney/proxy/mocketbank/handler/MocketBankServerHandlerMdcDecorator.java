package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.proxy_provider.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
public class MocketBankServerHandlerMdcDecorator implements ProviderProxySrv.Iface {

    private final MocketBankServerHandler handler;

    public MocketBankServerHandlerMdcDecorator(final MocketBankServerHandler rtn) {
        this.handler = rtn;
    }

    public void mdcPut(PaymentContext context) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        String paymentId = context.getPaymentInfo().getPayment().getId();
        MDC.put("invoiceId", invoiceId);
        MDC.put("paymentId", paymentId);
    }

    public void mdcRemove() {
        MDC.remove("invoiceId");
        MDC.remove("paymentId");
    }

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        RecurrentTokenProxyResult proxyResult = handler.generateToken(context);
        return proxyResult;
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer callback, RecurrentTokenContext context) throws TException {
        RecurrentTokenCallbackResult result = handler.handleRecurrentTokenCallback(callback, context);
        return result;
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        mdcPut(context);
        try {
            PaymentProxyResult proxyResult = handler.processPayment(context);
            return proxyResult;
        } catch (Exception ex) {
            String message = "Exception in processPayment with invoiceId " + context.getPaymentInfo().getInvoice().getId();
            log.error(message, ex);
            throw ex;
        } finally {
            mdcRemove();
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        mdcPut(context);
        try {
            PaymentCallbackResult result = handler.handlePaymentCallback(callback, context);
            return result;
        } catch (Exception ex) {
            String message = "handlePaymentCallback: Exception";
            log.error(message, ex);
            throw ex;
        } finally {
            mdcRemove();
        }
    }
}
