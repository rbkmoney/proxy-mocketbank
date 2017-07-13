package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.ExpDate;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy.Intent;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.proxy_provider.Invoice;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.Shop;

import java.nio.ByteBuffer;
import java.util.Map;

public class ProxyProviderWrapper {

    public static TargetInvoicePaymentStatus makeTargetProcessed() {
        TargetInvoicePaymentStatus target = new TargetInvoicePaymentStatus();
        target.setProcessed(new InvoicePaymentProcessed());
        return target;
    }

    public static TargetInvoicePaymentStatus makeTargetCaptured() {
        TargetInvoicePaymentStatus target = new TargetInvoicePaymentStatus();
        target.setCaptured(new InvoicePaymentCaptured());
        return target;
    }

    public static TargetInvoicePaymentStatus makeTargetCancelled() {
        TargetInvoicePaymentStatus target = new TargetInvoicePaymentStatus();
        target.setCancelled(new InvoicePaymentCancelled());
        return target;
    }

    public static Session makeSession(TargetInvoicePaymentStatus target, byte[] state) {
        Session session = new Session();
        session.setTarget(target);
        session.setState(state);
        return session;
    }

    public static Session makeSession(TargetInvoicePaymentStatus target) {
        return ProxyProviderWrapper.makeSession(target, null);
    }

    // ProxyResult
    public static ProxyResult makeProxyResult(Intent intent, byte[] next_state, TransactionInfo trx) {
        ProxyResult proxyResult = new ProxyResult();
        proxyResult.setIntent(intent);
        proxyResult.setNextState(next_state);
        proxyResult.setTrx(trx);
        return proxyResult;
    }

    public static ProxyResult makeProxyResult(Intent intent, byte[] next_state) {
        return makeProxyResult(intent, next_state, null);
    }

    public static ProxyResult makeProxyResult(Intent intent) {
        return makeProxyResult(intent, null, null);
    }

    public static ProxyResult makeProxyResultFailure(String code, String description) {
        ProxyResult proxyResult = new ProxyResult();
        proxyResult.setIntent(ProxyWrapper.makeFinishIntentFailure(code, description));
        return proxyResult;
    }

    public static Currency makeCurrency(String name, short numericCode, String symbolicCode, short exponent) {
        return DomainWrapper.makeCurrency(name, numericCode, symbolicCode, exponent);
    }

    public static ExpDate makeExpDate(byte month, short year) {
        return CdsWrapper.makeExpDate(month, year);
    }

    public static com.rbkmoney.damsel.proxy_provider.Cash makeCash(Currency currency, Long amount) {
        com.rbkmoney.damsel.proxy_provider.Cash cash = new com.rbkmoney.damsel.proxy_provider.Cash();
        cash.setAmount(amount);
        cash.setCurrency(currency);
        return cash;
    }

    public static CardData makeCardData(String cardholderName, String cvv, String pan, ExpDate expDate) {
        return CdsWrapper.makeCardData(cardholderName, cvv, pan, expDate);
    }

    public static BankCard makeBankCard(String bin, String maskedPan, String token, BankCardPaymentSystem bankCardPaymentSystem) {
        return DomainWrapper.makeBankCard(bin, maskedPan, token, bankCardPaymentSystem);
    }

        public static PaymentInfo makePaymentInfo(
            com.rbkmoney.damsel.proxy_provider.Invoice invoice,
            com.rbkmoney.damsel.proxy_provider.Shop shop,
            com.rbkmoney.damsel.proxy_provider.InvoicePayment invoicePayment
    ) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setInvoice(invoice);
        paymentInfo.setShop(shop);
        paymentInfo.setPayment(invoicePayment);
        return paymentInfo;
    }

    public static Context makeContext(
            com.rbkmoney.damsel.proxy_provider.PaymentInfo paymentInfo,
            com.rbkmoney.damsel.proxy_provider.Session session,
            Map<String, String> options
    ) {
        Context context = new Context();
        context.setPaymentInfo(paymentInfo);
        context.setSession(session);
        context.setOptions(options);
        return context;
    }

    public static Shop makeShop(Category category, ShopDetails shopDetails) {
        Shop shop = new Shop();
        shop.setCategory(category);
        shop.setDetails(shopDetails);
        return shop;
    }

    public static Invoice makeInvoice(String invoiceID, String createdAt, com.rbkmoney.damsel.proxy_provider.Cash cost) {
        Invoice invoice = new Invoice();
        invoice.setId(invoiceID);
        invoice.setCreatedAt(createdAt);
        invoice.setCost(cost);
        return invoice;
    }


    public static InvoicePayment makeInvoicePayment(String invoicePaymentId, String created_at, com.rbkmoney.damsel.domain.Payer payer, com.rbkmoney.damsel.proxy_provider.Cash cost) {
        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setId(invoicePaymentId);
        invoicePayment.setCreatedAt(created_at);
        invoicePayment.setPayer(payer);
        invoicePayment.setCost(cost);
        return invoicePayment;
    }

    public static InvoicePayment makeInvoicePaymentWithTrX(String invoicePaymentId, String created_at, com.rbkmoney.damsel.domain.Payer payer, com.rbkmoney.damsel.proxy_provider.Cash cost, TransactionInfo transactionInfo) {
        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setId(invoicePaymentId);
        invoicePayment.setCreatedAt(created_at);
        invoicePayment.setPayer(payer);
        invoicePayment.setCost(cost);
        invoicePayment.setTrx(transactionInfo);
        return invoicePayment;
    }

    public static com.rbkmoney.damsel.proxy_provider.Session makeSession(byte[] state) {
        com.rbkmoney.damsel.proxy_provider.Session session = new com.rbkmoney.damsel.proxy_provider.Session();
        session.setState(state);
        return session;
    }

    public static com.rbkmoney.damsel.proxy_provider.Session makeSession(ByteBuffer state) {
        com.rbkmoney.damsel.proxy_provider.Session session = new com.rbkmoney.damsel.proxy_provider.Session();
        session.setState(state);
        return session;
    }

    public static CallbackResult makeCallbackResult(byte[] callbackResponse, ProxyResult proxyResult) {
        CallbackResult callbackResult = new CallbackResult();
        callbackResult.setResponse(callbackResponse);
        callbackResult.setResult(proxyResult);
        return callbackResult;
    }


    public static CallbackResult makeCallbackResultFailure(byte[] callbackResponse, String code, String description) {
        CallbackResult callbackResult = new CallbackResult();
        callbackResult.setResponse(callbackResponse);
        callbackResult.setResult(ProxyProviderWrapper.makeProxyResultFailure(code, description));
        return callbackResult;
    }

}
