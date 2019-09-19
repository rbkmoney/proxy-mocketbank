package com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal;

import com.rbkmoney.damsel.domain.Currency;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.*;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsDomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsProviderAdapterWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.DateTimeUtils.getCurrentDateTimeByPattern;
import static com.rbkmoney.proxy.mocketbank.utils.withdrawal.CurrencyConverter.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalHandler {

    public ProcessResult handler(Withdrawal withdrawal, Value state, Map<String, String> options) throws TException {

        return WithdrawalsProviderAdapterWrapper.makeProcessResult(
                WithdrawalsProviderAdapterWrapper.makeFinishIntentSuccess(
                        WithdrawalsDomainWrapper.makeTransactionInfo(
                                withdrawal.getId()
                        )
                )
        );
    }

    public Quote getQuote(GetQuoteParams getQuoteParams, Map<String, String> options) {

        validateRequest(getQuoteParams);

        Quote quote = new Quote();

        Cash cashFrom = new Cash();
        Currency currencyFrom = getQuoteParams.getCurrencyFrom();
        cashFrom.setCurrency(currencyFrom);

        Cash cashTo = new Cash();
        Currency currencyTo = getQuoteParams.getCurrencyTo();
        cashTo.setCurrency(currencyTo);

        // Crypto currency or not? How check?
        if (isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount());
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
        } else {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount());
        }

        quote.setCashTo(cashTo);
        quote.setCashFrom(cashFrom);

        Value quoteData = new Value();
        quoteData.setStr(getQuoteParams.getIdempotencyId());
        quote.setQuoteData(quoteData);

        String createdAt = getCurrentDateTimeByPattern(Instant.EPOCH.toEpochMilli());
        String expiresOn = getCurrentDateTimeByPattern(Instant.EPOCH.plus(15, ChronoUnit.MINUTES).toEpochMilli());
        quote.setCreatedAt(createdAt);
        quote.setExpiresOn(expiresOn);

        return quote;
    }

    private void validateRequest(GetQuoteParams getQuoteParams) {
        if (isCryptoCurrency(getQuoteParams.getCurrencyFrom()) && isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            throw new RuntimeException("Can't exchange crypto currency to crypto currency");
        }

        if (isCurrencyEquals(getQuoteParams.getCurrencyFrom(), getQuoteParams.getCurrencyTo())) {
            throw new RuntimeException("Can't exchange equals currency");
        }

        if (!isExchangeCurrencyEqualsOtherCurrency(getQuoteParams)) {
            throw new RuntimeException("Can't exchange equals currency, all currency is different");
        }
    }

}