package com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.*;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsDomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsProviderAdapterWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.DateTimeUtils.getCurrentDateTimeByPattern;


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
        Quote quote = new Quote();

        Cash cashFrom = new Cash();
        Cash cashTo = new Cash();

        // Crypto currency or not? How check?
        cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount());
        cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
        if (isExchangeCurrencyTo(getQuoteParams)) {
            cashFrom.setCurrency(getQuoteParams.getCurrencyFrom());
            cashTo.setCurrency(getQuoteParams.getCurrencyTo());
        } else {
            cashTo.setCurrency(getQuoteParams.getCurrencyFrom());
            cashFrom.setCurrency(getQuoteParams.getCurrencyTo());
        }
        quote.setCashTo(cashTo);
        quote.setCashFrom(cashFrom);

        Value quoteData = new Value();
        quoteData.setStr(getQuoteParams.getIdempotencyId());
        quote.setQuoteData(quoteData);

        String date = getCurrentDateTimeByPattern(Instant.EPOCH.toString());
        quote.setCreatedAt(date);
        quote.setExpiresOn(date);

        return quote;
    }

    public static boolean isExchangeCurrencyTo(GetQuoteParams getQuoteParams) {
        return getQuoteParams.getExchangeCash().getCurrency().getSymbolicCode().equalsIgnoreCase(getQuoteParams.getCurrencyTo().getSymbolicCode());
    }

}