package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.domain.*;

import java.util.Map;

public class DomainWrapper {

    public static Currency makeCurrency(String name, short numericCode, String SymbolicCode, short exponent) {
        Currency currency = new Currency();
        currency.setName(name);
        currency.setNumericCode(numericCode);
        currency.setSymbolicCode(SymbolicCode);
        currency.setExponent(exponent);
        return currency;
    }

    public static BankCard makeBankCard(String bin, String maskedPan, String token, BankCardPaymentSystem bankCardPaymentSystem) {
        BankCard bankCard = new BankCard();
        bankCard.setBin(bin);
        bankCard.setMaskedPan(maskedPan);
        bankCard.setToken(token);
        bankCard.setPaymentSystem(bankCardPaymentSystem);
        return bankCard;
    }

    // TransactionInfo
    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra, String timestamp) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setId(paymentId);
        transactionInfo.setExtra(extra);
        transactionInfo.setTimestamp(timestamp);
        return transactionInfo;
    }

    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setId(paymentId);
        transactionInfo.setExtra(extra);
        transactionInfo.setTimestamp(null);
        return transactionInfo;
    }

    public static PaymentTool makePaymentTool(BankCard bankCard) {
        PaymentTool paymentTool = new PaymentTool();
        paymentTool.setBankCard(bankCard);
        return paymentTool;
    }

    public static DisposablePaymentResource makeDisposablePaymentResource(ClientInfo clientInfo, String paymentSessionId, PaymentTool paymentTool) {
        DisposablePaymentResource resource = new DisposablePaymentResource();
        resource.setClientInfo(clientInfo);
        resource.setPaymentSessionId(paymentSessionId);
        resource.setPaymentTool(paymentTool);
        return resource;
    }

    public static PaymentResourcePayer makePaymentResourcePayer(ContactInfo contactInfo, DisposablePaymentResource disposablePaymentResource) {
        PaymentResourcePayer paymentResourcePayer = new PaymentResourcePayer();
        paymentResourcePayer.setContactInfo(contactInfo);
        paymentResourcePayer.setResource(disposablePaymentResource);
        return paymentResourcePayer;
    }

    public static Payer makePayer(PaymentResourcePayer paymentResourcePayer) {
        Payer payer = new Payer();
        payer.setPaymentResource(paymentResourcePayer);
        return payer;
    }

    public static ClientInfo makeClientInfo(String fingerprint, String ipAddress) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setFingerprint(fingerprint);
        clientInfo.setIpAddress(ipAddress);
        return clientInfo;
    }

    public static ContactInfo makeContactInfo(String email, String phoneNumber) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(email);
        contactInfo.setPhoneNumber(phoneNumber);
        return contactInfo;
    }

    public static ShopLocation makeShopLocation(String url) {
        ShopLocation shopLocation = new ShopLocation();
        shopLocation.setUrl(url);
        return shopLocation;
    }

    public static ShopDetails makeShopDetails(String name, String description) {
        ShopDetails shopDetails = new ShopDetails();
        shopDetails.setName(name);

        return shopDetails;
    }

    public static Category makeCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

}
