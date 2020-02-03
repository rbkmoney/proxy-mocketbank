package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WithdrawalServerHandlerLog implements AdapterSrv.Iface {

    private final AdapterSrv.Iface handler;

    @Override
    public ProcessResult processWithdrawal(Withdrawal withdrawal, Value state, Map<String, String> options) throws TException {
        String withdrawalId = withdrawal.getId();
        log.info("processWithdrawal: start with withdrawalId='{}'", withdrawalId);
        try {
            ProcessResult processResult = handler.processWithdrawal(withdrawal, state, options);
            log.info("processWithdrawal: finish {} with withdrawalId='{}'", processResult, withdrawalId);
            return processResult;
        } catch (Exception ex) {
            String message = String.format("Failed processWithdrawal with withdrawalId='%s'", withdrawalId);
            ServerHandlerLogUtils.logMessage(ex, message);
            throw ex;
        }
    }

    @Override
    public Quote getQuote(GetQuoteParams params, Map<String, String> options) throws TException {
        String idempotencyId = params.getIdempotencyId();
        log.info("getQuote: start with idempotencyId='{}'", idempotencyId);
        try {
            Quote quote = handler.getQuote(params, options);
            log.info("getQuote: finish {} with idempotencyId='{}'", quote, idempotencyId);
            return quote;
        } catch (Exception ex) {
            String message = String.format("Failed getQuote with idempotencyId='%s'", idempotencyId);
            ServerHandlerLogUtils.logMessage(ex, message);
            throw ex;
        }
    }

}
