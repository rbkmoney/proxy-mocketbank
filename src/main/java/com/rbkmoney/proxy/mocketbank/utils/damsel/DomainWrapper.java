package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.base.Rational;
import com.rbkmoney.damsel.domain.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DomainWrapper {

    public static Cash makeCash(Currency currency, Long amount) {
        Cash cash = new Cash();
        cash.setAmount(amount);
        cash.setCurrency(currency);
        return cash;
    }

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

    public static InvoicePaymentProcessed makeInvoicePaymentProcessed() {
        return new InvoicePaymentProcessed();
    }

    public static InvoicePaymentCaptured makeInvoicePaymentCaptured() {
        return new InvoicePaymentCaptured();
    }

    public static InvoicePaymentCancelled makeInvoicePaymentCancelled() {
        return new InvoicePaymentCancelled();
    }

    public static InvoicePaymentFailed makeInvoicePaymentFailed() {
        return new InvoicePaymentFailed();
    }

    public static InvoicePaymentPending makeInvoicePaymentPending() {
        return new InvoicePaymentPending();
    }

    // InvoicePaymentStatus
    public static InvoicePaymentStatus makeInvoicePaymentStatusPending() {
        return InvoicePaymentStatus.pending(DomainWrapper.makeInvoicePaymentPending());
    }

    public static InvoicePaymentStatus makeInvoicePaymentStatusProcessed() {
        return InvoicePaymentStatus.processed(DomainWrapper.makeInvoicePaymentProcessed());
    }

    public static InvoicePaymentStatus makeInvoicePaymentStatusCaptured() {
        return InvoicePaymentStatus.captured(DomainWrapper.makeInvoicePaymentCaptured());
    }

    public static InvoicePaymentStatus makeInvoicePaymentStatusCanceled() {
        return InvoicePaymentStatus.cancelled(DomainWrapper.makeInvoicePaymentCancelled());
    }

    public static InvoicePaymentStatus makeInvoicePaymentStatusFailed() {
        return InvoicePaymentStatus.failed(DomainWrapper.makeInvoicePaymentFailed());
    }

    // InvoiceStatus
    public static InvoiceStatus makeInvoiceStatusUnpaid() {
        return InvoiceStatus.unpaid(new InvoiceUnpaid());
    }

    public static InvoiceStatus makeInvoiceStatusPaid() {
        return InvoiceStatus.paid(new InvoicePaid());
    }

    public static InvoiceStatus makeInvoiceStatusCancelled() {
        return InvoiceStatus.cancelled(new InvoiceCancelled());
    }

    public static InvoiceStatus makeInvoiceStatusFulfilled() {
        return InvoiceStatus.fulfilled(new InvoiceFulfilled());
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

    public static Payer makePayer(ContactInfo contactInfo, ClientInfo clientInfo, PaymentTool paymentTool, String session) {
        Payer payer = new Payer();
        payer.setContactInfo(contactInfo);
        payer.setClientInfo(clientInfo);
        payer.setPaymentTool(paymentTool);
        payer.setSession(session);
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

    public static PaymentTool makePaymentTool(BankCard bankCard) {
        PaymentTool paymentTool = new PaymentTool();
        paymentTool.setBankCard(bankCard);
        return paymentTool;
    }

    public static AmountLimit makeAmountLimit(AmountBound min, AmountBound max) {
        AmountLimit amountLimit = new AmountLimit();
        amountLimit.setMin(min);
        amountLimit.setMax(max);
        return amountLimit;
    }

    public static CashVolumeShare makeCashVolumeShare(Rational parts, CashFlowConstant cashFlowConstant) {
        CashVolumeShare cashVolumeShare = new CashVolumeShare();
        cashVolumeShare.setParts(parts);
        cashVolumeShare.setOf(cashFlowConstant);
        return cashVolumeShare;
    }

    public static CashVolume makeCashVolumeOnlyShare(CashVolumeShare cashVolumeShare) {
        CashVolume cashVolume = new CashVolume();
        cashVolume.setShare(cashVolumeShare);
        return cashVolume;
    }

    public static CashVolume makeCashVolumeOnlyFixed(CashVolumeFixed cashVolumeFixed) {
        CashVolume cashVolume = new CashVolume();
        cashVolume.setFixed(cashVolumeFixed);
        return cashVolume;
    }

    public static TerminalAccountSet makeTerminalAccountSet(CurrencyRef currencyRef, long receipt, long compensation) {
        TerminalAccountSet terminalAccountSet = new TerminalAccountSet();
        terminalAccountSet.setCurrency(currencyRef);
        terminalAccountSet.setReceipt(receipt);
        terminalAccountSet.setCompensation(compensation);
        return terminalAccountSet;
    }

    public static Terminal makeTerminal(String name, String description, CategoryRef categoryRef, PaymentMethodRef paymentMethodRef, List<CashFlowPosting> cashFlowPostings, TerminalAccountSet terminalAccountSet, Map<String, String> options) {
        Terminal terminal = new Terminal();
        terminal.setName(name);
        terminal.setDescription(description);
        terminal.setCategory(categoryRef);
        terminal.setPaymentMethod(paymentMethodRef);
        terminal.setCashFlow(cashFlowPostings);
        terminal.setAccounts(terminalAccountSet);
        terminal.setOptions(options);
        return terminal;
    }

    public static Provider makeProvider(String name, String description, TerminalSelector terminalSelector, Proxy proxy) {
        Provider provider = new Provider();
        provider.setName(name);
        provider.setDescription(description);
        provider.setTerminal(terminalSelector);
        provider.setProxy(proxy);
        return provider;
    }

    public static TerminalSelector makeTerminalSelector(Set<TerminalRef> terminalRefs) {
        TerminalSelector terminalSelector = new TerminalSelector();
        terminalSelector.setValue(terminalRefs);
        return terminalSelector;
    }

    public static Proxy makeProxy(ProxyRef ref, Map<String, String> additional) {
        Proxy proxy = new Proxy();
        proxy.setRef(ref);
        proxy.setAdditional(additional);
        return proxy;
    }

    public static Condition makeCondition(CurrencyRef currencyRef) {
        Condition condition = new Condition();
        condition.setCurrencyIs(currencyRef);
        return condition;
    }

    public static Predicate makePredicate(Condition condition) {
        Predicate predicate = new Predicate();
        predicate.setCondition(condition);
        return predicate;
    }

    public static CashFlowAccount makeCashFlowAccount(CashFlowParty cashFlowParty, String designation) {
        CashFlowAccount cashFlowAccount = new CashFlowAccount();
        cashFlowAccount.setParty(cashFlowParty);
        cashFlowAccount.setDesignation(designation);
        return cashFlowAccount;
    }

    public static CashFlowPosting makeCashFlowPosting(CashFlowAccount source, CashFlowAccount destination, CashVolume cashVolume) {
        CashFlowPosting cashFlowPosting = new CashFlowPosting();
        cashFlowPosting.setSource(source);
        cashFlowPosting.setDestination(destination);
        cashFlowPosting.setVolume(cashVolume);
        return cashFlowPosting;
    }

    public static CashFlowPosting makeCashFlowPosting(CashFlowAccount cashFlowAccount) {
        CashFlowPosting cashFlowPosting = new CashFlowPosting();
        cashFlowPosting.setDestination(cashFlowAccount);
        return cashFlowPosting;
    }

    public static CashFlowPosting makeCashFlowPostingDestination(CashVolume cashVolume) {
        CashFlowPosting cashFlowPosting = new CashFlowPosting();
        cashFlowPosting.setVolume(cashVolume);
        return cashFlowPosting;
    }


    public static SystemAccountSetSelector makeSystemAccountSetSelector(Set<SystemAccountSetRef> value) {
        SystemAccountSetSelector systemAccountSetSelector = new SystemAccountSetSelector();
        systemAccountSetSelector.setValue(value);
        return systemAccountSetSelector;
    }

    public static ProviderSelector makeProviderSelector(Set<ProviderRef> value) {
        ProviderSelector providerSelector = new ProviderSelector();
        providerSelector.setValue(value);
        return providerSelector;
    }

    public static CashFlowSelector makeCashFlowSelectorPredicate(Set<CashFlowPredicate> cashFlowPredicatesList) {
        CashFlowSelector cashFlowSelector = new CashFlowSelector();
        cashFlowSelector.setPredicates(cashFlowPredicatesList);
        return cashFlowSelector;
    }

    public static CashFlowPredicate makeCashFlowPredicate(Predicate _if, CashFlowSelector then) {
        CashFlowPredicate cashFlowPredicate = new CashFlowPredicate();
        cashFlowPredicate.setIf(_if);
        cashFlowPredicate.setThen(then);
        return cashFlowPredicate;
    }

    public static AmountLimitPredicate makeAmountLimitPredicate(Predicate _if, AmountLimitSelector then) {
        AmountLimitPredicate amountLimitPredicate = new AmountLimitPredicate();
        amountLimitPredicate.setIf(_if);
        amountLimitPredicate.setThen(then);
        return amountLimitPredicate;
    }

    public static AmountLimitSelector makeAmountLimitSelectorPredicate(Set<AmountLimitPredicate> predicates) {
        AmountLimitSelector amountLimitSelector = new AmountLimitSelector();
        amountLimitSelector.setPredicates(predicates);
        return amountLimitSelector;
    }

    public static AmountLimitSelector makeAmountLimitSelectorValue(AmountLimit value) {
        AmountLimitSelector amountLimitSelector = new AmountLimitSelector();
        amountLimitSelector.setValue(value);
        return amountLimitSelector;
    }

    public static PaymentMethodSelector makePaymentMethodSelector(Set<PaymentMethodRef> paymentMethodRefs) {
        PaymentMethodSelector paymentMethodSelector = new PaymentMethodSelector();
        paymentMethodSelector.setValue(paymentMethodRefs);
        return paymentMethodSelector;
    }

    public static ShopServices makeShopServices(PaymentsService paymentsService) {
        ShopServices shopServices = new ShopServices();
        shopServices.setPayments(paymentsService);
        return shopServices;
    }

    public static PaymentsService makePaymentsService(long domain_revision, PaymentsServiceTermsRef paymentsServiceTermsRef) {
        PaymentsService paymentsService = new PaymentsService();
        paymentsService.setDomainRevision(domain_revision);
        paymentsService.setTerms(paymentsServiceTermsRef);
        return paymentsService;
    }

    public static PaymentsServiceTermsRef makePaymentsServiceTermsRef(int id) {
        PaymentsServiceTermsRef paymentsServiceTermsRef = new PaymentsServiceTermsRef();
        paymentsServiceTermsRef.setId(id);
        return paymentsServiceTermsRef;
    }

    public static PaymentsServiceTerms makePaymentsServiceTerms(PaymentMethodSelector paymentMethodSelector, AmountLimitSelector amountLimitSelector, CashFlowSelector cashFlowSelector) {
        PaymentsServiceTerms paymentsServiceTerms = new PaymentsServiceTerms();
        paymentsServiceTerms.setPaymentMethods(paymentMethodSelector);
        paymentsServiceTerms.setLimits(amountLimitSelector);
        paymentsServiceTerms.setFees(cashFlowSelector);
        return paymentsServiceTerms;
    }


    public static DomainObject makeDomainObjectPaymentsServiceTermsObject(PaymentsServiceTermsRef ref, PaymentsServiceTerms data) {
        DomainObject domainObject = new DomainObject();
        PaymentsServiceTermsObject paymentsServiceTermsObject = new PaymentsServiceTermsObject();
        paymentsServiceTermsObject.setRef(ref);
        paymentsServiceTermsObject.setData(data);
        domainObject.setPaymentsServiceTerms(paymentsServiceTermsObject);
        return domainObject;
    }

    public static Globals makeGlobals(ProviderSelector providerSelector, PartyPrototypeRef partyPrototypeRef, SystemAccountSetSelector systemAccountSetSelector) {
        Globals globals = new Globals();
        globals.setProviders(providerSelector);
        globals.setPartyPrototype(partyPrototypeRef);
        globals.setSystemAccounts(systemAccountSetSelector);
        return globals;
    }

    public static PartyPrototype makePartyPrototype(ShopPrototype shopPrototype, ShopServices shopServices) {
        PartyPrototype partyPrototype = new PartyPrototype();
        partyPrototype.setShop(shopPrototype);
        partyPrototype.setDefaultServices(shopServices);
        return partyPrototype;
    }

    public static ShopPrototype makeShopPrototype(CurrencyRef currencyRef, CategoryRef categoryRef, ShopDetails shopDetails) {
        ShopPrototype shopPrototype = new ShopPrototype();
        shopPrototype.setCurrency(currencyRef);
        shopPrototype.setCategory(categoryRef);
        shopPrototype.setDetails(shopDetails);
        return shopPrototype;
    }

    public static DomainObject makeDomainObjectProxyObject(ProxyRef ref, ProxyDefinition data) {
        DomainObject domainObject = new DomainObject();
        ProxyObject proxyObject = new ProxyObject();
        proxyObject.setRef(ref);
        proxyObject.setData(data);
        domainObject.setProxy(proxyObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectProviderObject(ProviderRef ref, Provider data) {
        DomainObject domainObject = new DomainObject();
        ProviderObject providerObject = new ProviderObject();
        providerObject.setRef(ref);
        providerObject.setData(data);
        domainObject.setProvider(providerObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectCategoryObject(CategoryRef ref, Category data) {
        DomainObject domainObject = new DomainObject();
        CategoryObject categoryObject = new CategoryObject();
        categoryObject.setRef(ref);
        categoryObject.setData(data);
        domainObject.setCategory(categoryObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectCurrencyObject(CurrencyRef ref, Currency data) {
        DomainObject domainObject = new DomainObject();
        CurrencyObject currencyObject = new CurrencyObject();
        currencyObject.setRef(ref);
        currencyObject.setData(data);
        domainObject.setCurrency(currencyObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectPartyPrototypeObject(PartyPrototypeRef ref, PartyPrototype data) {
        DomainObject domainObject = new DomainObject();
        PartyPrototypeObject partyPrototypeObject = new PartyPrototypeObject();
        partyPrototypeObject.setRef(ref);
        partyPrototypeObject.setData(data);
        domainObject.setPartyPrototype(partyPrototypeObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectGlobalsObject(GlobalsRef ref, Globals data) {
        DomainObject domainObject = new DomainObject();
        GlobalsObject globalsObject = new GlobalsObject();
        globalsObject.setRef(ref);
        globalsObject.setData(data);
        domainObject.setGlobals(globalsObject);
        return domainObject;
    }

    public static DomainObject makeDomainObjectTerminalObject(TerminalRef ref, Terminal data) {
        DomainObject domainObject = new DomainObject();
        TerminalObject terminalObject = new TerminalObject();
        terminalObject.setRef(ref);
        terminalObject.setData(data);
        domainObject.setTerminal(terminalObject);
        return domainObject;
    }

    public static GlobalsRef makeGlobalsRef() {
        return new GlobalsRef();
    }

    public static TerminalRef makeTerminalRef(int id) {
        TerminalRef terminalRef = new TerminalRef();
        terminalRef.setId(id);
        return terminalRef;
    }

    public static PaymentMethodRef makePaymentMethodRef(BankCardPaymentSystem bankCardPaymentSystem) {
        PaymentMethodRef paymentMethodRef = new PaymentMethodRef();
        paymentMethodRef.setId(DomainWrapper.makePaymentMethod(bankCardPaymentSystem));
        return paymentMethodRef;
    }

    public static ShopDetails makeShopDetails(String name, String description, String location) {
        ShopDetails shopDetails = new ShopDetails();
        shopDetails.setName(name);
        shopDetails.setDescription(description);
        shopDetails.setLocation(location);
        return shopDetails;
    }

    public static ShopDetails makeShopDetails(String name, String description) {
        return DomainWrapper.makeShopDetails(name, description, null);
    }

    public static Category makeCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    public static PaymentMethod makePaymentMethod(BankCardPaymentSystem bankCardPaymentSystem) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setBankCard(bankCardPaymentSystem);
        return paymentMethod;
    }

    public static ProxyDefinition makeProxyDefinition(String url, Map<String, String> options) {
        ProxyDefinition proxyDefinition = new ProxyDefinition();
        proxyDefinition.setUrl(url);
        proxyDefinition.setOptions(options);
        return proxyDefinition;
    }

    public static ProxyDefinition makeProxyDefinition(String url) {
        return DomainWrapper.makeProxyDefinition(url, null);
    }

    public static PartyPrototypeRef makePartyPrototypeRef(int id) {
        PartyPrototypeRef partyPrototypeRef = new PartyPrototypeRef();
        partyPrototypeRef.setId(id);
        return partyPrototypeRef;
    }

    public static CategoryRef makeCategoryRef(int id) {
        CategoryRef categoryRef = new CategoryRef();
        categoryRef.setId(id);
        return categoryRef;
    }

    public static ProviderRef makeProviderRef(int id) {
        ProviderRef providerRef = new ProviderRef();
        providerRef.setId(id);
        return providerRef;
    }

    public static ProxyRef makeProxyRef(int id) {
        ProxyRef proxyRef = new ProxyRef();
        proxyRef.setId(id);
        return proxyRef;
    }

    public static CurrencyRef makeCurrencyRef(String symbolicCode) {
        CurrencyRef currencyRef = new CurrencyRef();
        currencyRef.setSymbolicCode(symbolicCode);
        return currencyRef;
    }

}
