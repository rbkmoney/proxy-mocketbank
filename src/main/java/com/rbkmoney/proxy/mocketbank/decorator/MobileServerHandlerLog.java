package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import com.rbkmoney.proxy.mocketbank.converter.PaymentResourceTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class MobileServerHandlerLog implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface handler;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        String recurrentId = ProxyProviderPackageExtractors.extractRecurrentId(context);
        log.info("GenerateToken started with recurrentId='{}'", recurrentId);
        try {
            RecurrentTokenProxyResult proxyResult = handler.generateToken(context);
            log.info("GenerateToken finished {} with recurrentId='{}'", proxyResult, recurrentId);
            return proxyResult;
        } catch (Exception ex) {
            String message = String.format("Failed handle GenerateToken with recurrentId='%s'", recurrentId);
            ServerHandlerLogUtils.logMessage(ex, message);
            throw ex;
        }
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer byteBuffer, RecurrentTokenContext context) throws TException {
        String recurrentId = ProxyProviderPackageExtractors.extractRecurrentId(context);
        log.info("HandleRecurrentTokenCallback: start with recurrentId='{}'", recurrentId);
        RecurrentTokenCallbackResult result = handler.handleRecurrentTokenCallback(byteBuffer, context);
        log.info("HandleRecurrentTokenCallback end {} with recurrentId='{}'", result, recurrentId);
        return result;
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        String invoiceId = ProxyProviderPackageExtractors.extractInvoiceId(context);
        String invoicePaymentStatus = ProxyProviderPackageExtractors.extractTargetInvoicePaymentStatus(context);
        String paymentResourceType = PaymentResourceTypeResolver.extractPaymentResourceType(context);
        log.info("Process payment handle resource='{}', status='{}' start with invoiceId='{}'", paymentResourceType, invoicePaymentStatus, invoiceId);
        try {
            PaymentProxyResult proxyResult = handler.processPayment(context);
            log.info("Process payment handle resource='{}', status='{}' finished with invoiceId='{}' and proxyResult='{}'",
                    paymentResourceType, invoicePaymentStatus, invoiceId, proxyResult);
            return proxyResult;
        } catch (Exception e) {
            String message = String.format("Failed handle resource='%s', status='%s' process payment for operation with invoiceId '%s'",
                    paymentResourceType, invoicePaymentStatus, invoiceId);
            ServerHandlerLogUtils.logMessage(e, message);
            throw e;
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext context) throws TException {
        String invoiceId = ProxyProviderPackageExtractors.extractInvoiceId(context);
        log.info("HandlePaymentCallback start with invoiceId='{}'", invoiceId);
        PaymentCallbackResult result = handler.handlePaymentCallback(byteBuffer, context);
        log.info("HandlePaymentCallback finish {} with invoiceId='{}'", result, invoiceId);
        return result;
    }
}
