package com.rbkmoney.proxy.mocketbank.service.oct.verification;

import com.rbkmoney.damsel.withdrawals.provider_adapter.GetQuoteParams;
import com.rbkmoney.proxy.mocketbank.service.oct.constant.CryptoCurrencies;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyVerification {

    public static boolean isCryptoCurrency(com.rbkmoney.damsel.domain.Currency currency) {
        return Arrays.asList(CryptoCurrencies.toArraySymbol()).contains(currency.getSymbolicCode());
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
