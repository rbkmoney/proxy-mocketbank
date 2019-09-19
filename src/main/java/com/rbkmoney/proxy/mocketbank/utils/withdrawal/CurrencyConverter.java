package com.rbkmoney.proxy.mocketbank.utils.withdrawal;

import com.rbkmoney.damsel.withdrawals.provider_adapter.GetQuoteParams;

import java.util.Arrays;

public class CurrencyConverter {

    private static final String[] CRYPTO_CURRENCY = new String[]{"BTC", "BCH", "LTC", "XRP", "ETH", "ZEC"};

    public static boolean isCryptoCurrency(com.rbkmoney.damsel.domain.Currency currency) {
        return Arrays.asList(CRYPTO_CURRENCY).contains(currency.getSymbolicCode());
    }

    public static boolean isCurrencyEquals(com.rbkmoney.damsel.domain.Currency currencyFrom, com.rbkmoney.damsel.domain.Currency currencyTo) {
        return currencyFrom.getSymbolicCode().equalsIgnoreCase(currencyTo.getSymbolicCode())
                || (currencyFrom.getNumericCode() == currencyTo.getNumericCode());
    }

    public static boolean isExchangeCurrencyEqualsOtherCurrency(GetQuoteParams getQuoteParams) {
        com.rbkmoney.damsel.domain.Currency currencyFrom = getQuoteParams.getCurrencyFrom();
        com.rbkmoney.damsel.domain.Currency currencyTo = getQuoteParams.getCurrencyTo();
        com.rbkmoney.damsel.domain.Currency exchangeCurrency = getQuoteParams.getExchangeCash().getCurrency();
        return isCurrencyEquals(currencyFrom, exchangeCurrency) || isCurrencyEquals(currencyTo, exchangeCurrency);
    }

}
