package com.rbkmoney.proxy.mocketbank.handler.digital.wallet;

import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.handler.digital.wallet.callback.DigitalWalletCallbackHandler;
import com.rbkmoney.proxy.mocketbank.validator.DigitalWalletValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalWalletServerHandler implements ProviderProxySrv.Iface {

    private final List<CommonDigitalWalletHandler> handlers;
    private final DigitalWalletCallbackHandler digitalWalletCallbackHandler;
    private final DigitalWalletValidator digitalWalletValidator;

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
        digitalWalletValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new UnsupportedDigitalWalletHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) {
        return digitalWalletCallbackHandler.handler(callback, context);
    }

}
