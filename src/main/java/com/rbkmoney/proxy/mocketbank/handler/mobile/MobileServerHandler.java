package com.rbkmoney.proxy.mocketbank.handler.mobile;

import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.handler.mobile.payment.UnsupportedMobileHandler;
import com.rbkmoney.proxy.mocketbank.validator.MobileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobileServerHandler implements ProviderProxySrv.Iface {

    private final MobileValidator mobileValidator;
    private final List<CommonMobileHandler> handlers;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        throw new TException("Method Not Supported");
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(
            ByteBuffer callback,
            RecurrentTokenContext context) throws TException {
        throw new TException("Method Not Supported");
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        mobileValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new UnsupportedMobileHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        throw new TException("Method Not Supported");
    }
}
