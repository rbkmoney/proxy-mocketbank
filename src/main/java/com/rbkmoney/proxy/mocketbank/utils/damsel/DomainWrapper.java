package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.domain.*;

import java.util.Map;

public class DomainWrapper {

    public static Currency makeCurrency(String name, short numericCode, String symbolicCode, short exponent) {
        return new Currency(name, symbolicCode, numericCode, exponent);
    }

    public static CurrencyRef makeCurrencyRef(String symbolicCode) {
        return new CurrencyRef(symbolicCode);
    }

    public static BankCard makeBankCard(String bin, String maskedPan, String token, BankCardPaymentSystem bankCardPaymentSystem) {
        return new BankCard(token, bankCardPaymentSystem, bin, maskedPan);
    }

    // TransactionInfo
    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra, String timestamp) {
        return new TransactionInfo(paymentId, extra).setTimestamp(timestamp);
    }

    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra) {
        return makeTransactionInfo(paymentId, extra, null);
    }

    public static PaymentTool makePaymentTool(BankCard bankCard) {
        return PaymentTool.bank_card(bankCard);
    }

    public static DisposablePaymentResource makeDisposablePaymentResource(ClientInfo clientInfo, String paymentSessionId, PaymentTool paymentTool) {
        return new DisposablePaymentResource(paymentTool).setClientInfo(clientInfo).setPaymentSessionId(paymentSessionId);
    }

    public static PaymentResourcePayer makePaymentResourcePayer(ContactInfo contactInfo, DisposablePaymentResource disposablePaymentResource) {
        return new PaymentResourcePayer(disposablePaymentResource, contactInfo);
    }

    public static Payer makePayer(PaymentResourcePayer paymentResourcePayer) {
        return Payer.payment_resource(paymentResourcePayer);
    }

    public static ClientInfo makeClientInfo(String fingerprint, String ipAddress) {
        return new ClientInfo().setFingerprint(fingerprint).setIpAddress(ipAddress);
    }

    public static ContactInfo makeContactInfo(String email, String phoneNumber) {
        return new ContactInfo().setEmail(email).setPhoneNumber(phoneNumber);
    }

    public static ContactInfo makeContactInfoWithEmail(String email) {
        return new ContactInfo().setEmail(email);
    }

    public static ContactInfo makeContactInfoWithPhoneNumber(String phoneNumber) {
        return new ContactInfo().setPhoneNumber(phoneNumber);
    }

    public static ShopLocation makeShopLocation(String url) {
        return ShopLocation.url(url);
    }

    public static ShopDetails makeShopDetails(String name, String description) {
        return new ShopDetails(name).setDescription(description);
    }

    public static Category makeCategory(String name, String description) {
        return new Category(name, description);
    }

}
