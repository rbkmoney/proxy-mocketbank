package com.rbkmoney.proxy.mocketbank.handler.payment;

import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.handler.payment.callback.PaymentCallbackHandler;
import com.rbkmoney.proxy.mocketbank.handler.payment.callback.RecurrentTokenCallbackHandler;
import com.rbkmoney.proxy.mocketbank.handler.payment.recurrent.GenerateTokenHandler;
import com.rbkmoney.proxy.mocketbank.validator.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentServerHandler implements ProviderProxySrv.Iface {

    private final List<CommonPaymentHandler> handlers;
    private final PaymentCallbackHandler paymentCallbackHandler;
    private final GenerateTokenHandler generateTokenHandler;
    private final RecurrentTokenCallbackHandler recurrentTokenCallbackHandler;
    private final PaymentValidator paymentValidator;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        return generateTokenHandler.handler(context);
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer callback, RecurrentTokenContext context) throws TException {
        return recurrentTokenCallbackHandler.handler(callback, context);
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        paymentValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new UnsupportedPaymentHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        return paymentCallbackHandler.handler(callback, context);
    }
}
