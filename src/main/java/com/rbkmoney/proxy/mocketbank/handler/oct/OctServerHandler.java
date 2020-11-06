package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.*;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators;
import com.rbkmoney.java.damsel.utils.creators.WithdrawalsProviderAdapterPackageCreators;
import com.rbkmoney.proxy.mocketbank.service.oct.verification.CurrencyVerification;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayout;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayoutAction;
import com.rbkmoney.proxy.mocketbank.utils.payout.PayoutUtils;
import com.rbkmoney.proxy.mocketbank.validator.WithdrawalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OctServerHandler implements AdapterSrv.Iface {

    private final CdsClientStorage cds;
    private final ErrorMapping errorMapping;
    private final List<CardPayout> cardPayoutList;
    private final WithdrawalValidator withdrawalValidator;

    @Override
    public ProcessResult processWithdrawal(Withdrawal withdrawal, Value state, Map<String, String> options) throws TException {

        if (withdrawal.getDestination().isSetBankCard()) {
            CardDataProxyModel cardData = cds.getCardData(withdrawal);
            Optional<CardPayout> cardPayout = PayoutUtils.extractCardPayoutByPan(cardPayoutList, cardData.getPan());
            if (cardPayout.isPresent()) {
                log.info("Found card payout with action {}", cardPayout.get().getAction());
                if(CardPayoutAction.isCardFailed(cardPayout.get())) {
                    CardPayoutAction action = CardPayoutAction.findByValue(cardPayout.get().getAction());
                    log.info("Failed card payout with action {}", action);
                    return ErrorBuilder.prepareWithdrawalError(errorMapping, action);
                }
            }
        }

        TransactionInfo transactionInfo = DomainPackageCreators.createTransactionInfo(withdrawal.getId(), Collections.emptyMap());
        Intent intent = WithdrawalsProviderAdapterPackageCreators.createFinishIntentSuccess(transactionInfo);
        return WithdrawalsProviderAdapterPackageCreators.createProcessResult(intent);
    }

    @Override
    public Quote getQuote(GetQuoteParams getQuoteParams, Map<String, String> options) {
        withdrawalValidator.validate(getQuoteParams, options);

        Cash cashFrom = new Cash().setCurrency(getQuoteParams.getCurrencyFrom());
        Cash cashTo = new Cash().setCurrency(getQuoteParams.getCurrencyTo());
        if (CurrencyVerification.isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount());
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
        } else {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount());
        }

        Value quoteData = new Value();
        quoteData.setStr(getQuoteParams.getIdempotencyId());
        return new Quote()
                .setCashTo(cashTo)
                .setCashFrom(cashFrom)
                .setQuoteData(quoteData)
                .setCreatedAt(getCurrentDateTimeByPattern(Instant.EPOCH.toEpochMilli()))
                .setExpiresOn(getCurrentDateTimeByPattern(Instant.EPOCH
                        .plus(15, ChronoUnit.MINUTES)
                        .toEpochMilli())
                );
    }

    @Override
    public CallbackResult handleCallback(Callback callback, Withdrawal withdrawal, Value value, Map<String, String> map) throws TException {
        throw new TException("Not supported method handleCallback");
    }

    private static String getCurrentDateTimeByPattern(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

}
