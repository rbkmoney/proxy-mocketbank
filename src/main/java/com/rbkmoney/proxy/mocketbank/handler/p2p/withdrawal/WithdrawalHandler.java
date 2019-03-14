package com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsDomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsProviderAdapterWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Map;


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

}