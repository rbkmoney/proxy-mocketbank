package com.rbkmoney.proxy.mocketbank.validator;

import com.rbkmoney.damsel.withdrawals.provider_adapter.GetQuoteParams;
import com.rbkmoney.proxy.mocketbank.exception.WithdrawalException;
import com.rbkmoney.proxy.mocketbank.handler.oct.converter.CurrencyConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WithdrawalValidator implements Validator<GetQuoteParams> {

    public void validate(GetQuoteParams getQuoteParams, Map<String, String> options) {
        validateRequiredFields(options);
        if (CurrencyConverter.isCryptoCurrency(getQuoteParams.getCurrencyFrom()) && CurrencyConverter.isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            throw new WithdrawalException("Can't exchange crypto currency to crypto currency");
        }
        if (CurrencyConverter.isCurrencyEquals(getQuoteParams.getCurrencyFrom(), getQuoteParams.getCurrencyTo())) {
            throw new WithdrawalException("Can't exchange equals currency");
        }
        if (!CurrencyConverter.isExchangeCurrencyEqualsOtherCurrency(getQuoteParams)) {
            throw new WithdrawalException("Can't exchange equals currency, all currency is different");
        }
    }

    /**
     * Fill if need. Example:
     *
     * <pre>
     * {@code
     *  Objects.requireNonNull(options.get("TERMINAL"), "Option 'TERMINAL' must be set");
     * }
     * </pre>
     *
     * @param options options for request
     */
    private void validateRequiredFields(Map<String, String> options) {
        // Not use yet
    }
}

