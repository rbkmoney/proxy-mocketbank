package com.rbkmoney.proxy.mocketbank.handler.mobile.payment;

import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnsupportedMobileHandler implements CommonMobileHandler {

    @Override
    public boolean filter(final TargetInvoicePaymentStatus targetInvoicePaymentStatus, final PaymentResource paymentResource) {
        return false;
    }

    @Override
    public PaymentProxyResult handler(final PaymentContext context) throws TException {
        throw new TException("Unsupported method");
    }

}
