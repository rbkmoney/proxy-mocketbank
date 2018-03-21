package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.base.Timer;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.ExpDate;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.proxy_provider.Invoice;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.Shop;
import com.rbkmoney.damsel.user_interaction.UserInteraction;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;

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

    public static TargetInvoicePaymentStatus makeTargetRefunded() {
        TargetInvoicePaymentStatus target = new TargetInvoicePaymentStatus();
        target.setRefunded(new InvoicePaymentRefunded());
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


    // RecurrentTokenIntent
    public static RecurrentTokenSuccess makeRecurrentTokenSuccess(String token) {
        RecurrentTokenSuccess recurrentTokenSuccess = new RecurrentTokenSuccess();
        recurrentTokenSuccess.setToken(token);
        return recurrentTokenSuccess;
    }

    public static RecurrentTokenFinishIntent makeRecurrentTokenStatusSuccess(String token) {
        RecurrentTokenFinishIntent intent = new RecurrentTokenFinishIntent();

        RecurrentTokenFinishStatus status = new RecurrentTokenFinishStatus();
        status.setSuccess(makeRecurrentTokenSuccess(token));
        intent.setStatus(status);

        return intent;
    }

    public static RecurrentTokenFinishIntent makeRecurrentTokenStatusFailure(String code, String description) {
        RecurrentTokenFinishStatus status = new RecurrentTokenFinishStatus();
        Failure failure = ErrorMapping.getInstance().getFailureByCodeAndDescription(code, description);
        status.setFailure(failure);

        RecurrentTokenFinishIntent intent = new RecurrentTokenFinishIntent();
        intent.setStatus(status);

        return intent;
    }

    public static RecurrentTokenIntent makeRecurrentTokenFinishIntentFailure(String code, String description) {
        RecurrentTokenIntent intent = new RecurrentTokenIntent();
        intent.setFinish(makeRecurrentTokenStatusFailure(code, description));
        return intent;
    }

    public static RecurrentTokenIntent makeRecurrentTokenFinishIntentSuccess(String token) {
        RecurrentTokenIntent intent = new RecurrentTokenIntent();
        intent.setFinish(makeRecurrentTokenStatusSuccess(token));
        return intent;
    }

    public static RecurrentTokenIntent makeRecurrentTokenWithSuspendIntent(String tag, Timer timer, UserInteraction userInteraction) {
        RecurrentTokenIntent intent = new RecurrentTokenIntent();
        intent.setSuspend(ProxyWrapper.makeSuspendIntent(tag, timer, userInteraction));
        return intent;
    }

    public static RecurrentTokenIntent makeRecurrentTokenWithSuspendIntent(String tag, Timer timer) {
        return makeRecurrentTokenWithSuspendIntent(tag, timer, null);
    }

    // RecurrentTokenInfo
    public static RecurrentTokenInfo makeRecurrentTokenInfo(RecurrentPaymentTool recurrentPaymentTool) {
        RecurrentTokenInfo recurrentTokenInfo = new RecurrentTokenInfo();
        recurrentTokenInfo.setPaymentTool(recurrentPaymentTool);
        return recurrentTokenInfo;
    }

    // DisposablePaymentResource
    public static DisposablePaymentResource makeDisposablePaymentResource(String sessionId, PaymentTool paymentTool) {
        DisposablePaymentResource disposablePaymentResource = new DisposablePaymentResource();
        disposablePaymentResource.setPaymentSessionId(sessionId);
        disposablePaymentResource.setPaymentTool(paymentTool);
        return disposablePaymentResource;
    }

    // RecurrentPaymentTool
    public static RecurrentPaymentTool makeRecurrentPaymentTool(DisposablePaymentResource disposablePaymentResource) {
        RecurrentPaymentTool recurrentPaymentTool = new RecurrentPaymentTool();
        recurrentPaymentTool.setPaymentResource(disposablePaymentResource);
        return recurrentPaymentTool;
    }

    public static RecurrentPaymentTool makeRecurrentPaymentTool(DisposablePaymentResource disposablePaymentResource, com.rbkmoney.damsel.proxy_provider.Cash cash) {
        RecurrentPaymentTool recurrentPaymentTool = new RecurrentPaymentTool();
        recurrentPaymentTool.setPaymentResource(disposablePaymentResource);
        recurrentPaymentTool.setMinimalPaymentCost(cash);
        return recurrentPaymentTool;
    }

    public static RecurrentPaymentTool makeRecurrentPaymentTool(String id, DisposablePaymentResource disposablePaymentResource, com.rbkmoney.damsel.proxy_provider.Cash cash) {
        RecurrentPaymentTool recurrentPaymentTool = new RecurrentPaymentTool();
        recurrentPaymentTool.setPaymentResource(disposablePaymentResource);
        recurrentPaymentTool.setMinimalPaymentCost(cash);
        recurrentPaymentTool.setId(id);
        return recurrentPaymentTool;
    }


    // RecurrentTokenProxyResult
    public static RecurrentTokenProxyResult makeRecurrentTokenProxyResult(
            RecurrentTokenIntent intent, byte[] nextState, TransactionInfo trx
    ) {
        RecurrentTokenProxyResult result = new RecurrentTokenProxyResult();
        result.setIntent(intent);
        result.setNextState(nextState);
        result.setTrx(trx);
        return result;
    }

    public static RecurrentTokenProxyResult makeRecurrentTokenProxyResult(RecurrentTokenIntent intent) {
        return makeRecurrentTokenProxyResult(intent, null,  null);
    }

    public static RecurrentTokenProxyResult makeRecurrentTokenProxyResult(
            RecurrentTokenIntent intent, byte[] nextState
    ) {
        return makeRecurrentTokenProxyResult(intent, nextState, null);
    }


    public static RecurrentTokenProxyResult makeRecurrentTokenProxyResultFailure(String code, String description) {
        return makeRecurrentTokenProxyResult(makeRecurrentTokenFinishIntentFailure(code, description));
    }

    // ProxyResult
    public static PaymentProxyResult makePaymentProxyResult(Intent intent, byte[] next_state, TransactionInfo trx) {
        PaymentProxyResult proxyResult = new PaymentProxyResult();
        proxyResult.setIntent(intent);
        proxyResult.setNextState(next_state);
        proxyResult.setTrx(trx);
        return proxyResult;
    }

    public static PaymentProxyResult makePaymentProxyResult(Intent intent, byte[] next_state) {
        return makePaymentProxyResult(intent, next_state, null);
    }

    public static PaymentProxyResult makePaymentProxyResult(Intent intent) {
        return makePaymentProxyResult(intent, null, null);
    }

    public static PaymentProxyResult makeProxyResultFailure(String code, String description) {
        PaymentProxyResult proxyResult = new PaymentProxyResult();
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

    public static com.rbkmoney.damsel.domain.Cash makeCash(CurrencyRef currency, Long amount) {
        com.rbkmoney.damsel.domain.Cash cash = new com.rbkmoney.damsel.domain.Cash();
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

    public static PaymentContext makeContext(
            com.rbkmoney.damsel.proxy_provider.PaymentInfo paymentInfo,
            com.rbkmoney.damsel.proxy_provider.Session session,
            Map<String, String> options
    ) {
        PaymentContext context = new PaymentContext();
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


    public static InvoicePayment makeInvoicePayment(String invoicePaymentId, String created_at, PaymentResource paymentResource, com.rbkmoney.damsel.proxy_provider.Cash cost) {
        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setId(invoicePaymentId);
        invoicePayment.setCreatedAt(created_at);
        invoicePayment.setPaymentResource(paymentResource);
        invoicePayment.setCost(cost);
        return invoicePayment;
    }

    public static PaymentResource makePaymentResourceDisposablePaymentResource(DisposablePaymentResource disposablePaymentResource) {
        PaymentResource paymentResource = new PaymentResource();
        paymentResource.setDisposablePaymentResource(disposablePaymentResource);
        return paymentResource;
    }

    public static RecurrentPaymentResource makeRecurrentPaymentResource(String token) {
        RecurrentPaymentResource resource = new RecurrentPaymentResource();
        resource.setRecToken(token);
        return resource;
    }

    public static PaymentResource makePaymentResourceRecurrentPaymentResource(RecurrentPaymentResource recurrentPaymentResource) {
        PaymentResource paymentResource = new PaymentResource();
        paymentResource.setRecurrentPaymentResource(recurrentPaymentResource);
        return paymentResource;
    }

    public static InvoicePayment makeInvoicePaymentWithTrX(String invoicePaymentId, String created_at, PaymentResource paymentResource, com.rbkmoney.damsel.proxy_provider.Cash cost, TransactionInfo transactionInfo) {
        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setId(invoicePaymentId);
        invoicePayment.setCreatedAt(created_at);
        invoicePayment.setPaymentResource(paymentResource);
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

    public static PaymentCallbackProxyResult makeCallbackProxyResult(Intent intent, byte[] next_state, TransactionInfo trx) {
        PaymentCallbackProxyResult proxyResult = new PaymentCallbackProxyResult();
        proxyResult.setIntent(intent);
        proxyResult.setNextState(next_state);
        proxyResult.setTrx(trx);
        return proxyResult;
    }

    public static PaymentCallbackProxyResult makeCallbackProxyResultFailure(String code, String description) {
        PaymentCallbackProxyResult proxyResult = new PaymentCallbackProxyResult();
        proxyResult.setIntent(ProxyWrapper.makeFinishIntentFailure(code, description));
        return proxyResult;
    }

    public static PaymentCallbackResult makeCallbackResult(byte[] callbackResponse, PaymentCallbackProxyResult proxyResult) {
        PaymentCallbackResult callbackResult = new PaymentCallbackResult();
        callbackResult.setResponse(callbackResponse);
        callbackResult.setResult(proxyResult);
        return callbackResult;
    }

    public static PaymentCallbackResult makeCallbackResultFailure(byte[] callbackResponse, String code, String description) {
        PaymentCallbackResult callbackResult = new PaymentCallbackResult();
        callbackResult.setResponse(callbackResponse);
        callbackResult.setResult(ProxyProviderWrapper.makeCallbackProxyResultFailure(code, description));
        return callbackResult;
    }

    public static PaymentCallbackResult makeCallbackResultFailure(String code, String description) {
        return makeCallbackResultFailure("error".getBytes(), code, description);
    }

    // RecurrentTokenCallbackResult
    public static RecurrentTokenCallbackResult makeRecurrentTokenCallbackResult(byte[] callbackResponse, RecurrentTokenProxyResult proxyResult) {
        RecurrentTokenCallbackResult result = new RecurrentTokenCallbackResult();
        result.setResponse(callbackResponse);
        result.setResult(proxyResult);
        return result;
    }

    public static RecurrentTokenCallbackResult makeRecurrentTokenCallbackResultFailure(byte[] callbackResponse, String code, String description) {
        RecurrentTokenCallbackResult result = new RecurrentTokenCallbackResult();
        result.setResponse(callbackResponse);
        result.setResult(makeRecurrentTokenProxyResult(makeRecurrentTokenFinishIntentFailure(code, description)));
        return result;
    }

    public static RecurrentTokenCallbackResult makeRecurrentTokenCallbackResultFailure(String code, String description) {
        return makeRecurrentTokenCallbackResultFailure("error".getBytes(), code, description);
    }

}
