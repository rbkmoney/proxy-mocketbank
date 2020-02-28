package com.rbkmoney.proxy.mocketbank.handler.terminal;

import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.handler.terminal.payment.UnsupportedTerminalHandler;
import com.rbkmoney.proxy.mocketbank.validator.TerminalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalServerHandler implements ProviderProxySrv.Iface {

    private final TerminalValidator terminalValidator;
    private final List<CommonTerminalHandler> handlers;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        throw new TException("Method Not Supported");
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer callback, RecurrentTokenContext context) throws TException {
        throw new TException("Method Not Supported");
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        terminalValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new UnsupportedTerminalHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        throw new TException("Method Not Supported");
    }
}
