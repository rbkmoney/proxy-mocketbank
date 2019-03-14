package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.AdapterSrv;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal.WithdrawalHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.utils.MocketBankUtils.isUndefinedResultOrUnavailable;

@Slf4j
@Component
@RequiredArgsConstructor
public class P2PCreditServerHandler  implements AdapterSrv.Iface  {

    private final WithdrawalHandler withdrawalHandler;

    @Override
    public ProcessResult processWithdrawal(Withdrawal withdrawal, Value state, Map<String, String> options) throws TException {
        String withdrawalId = withdrawal.getId();
        log.info("processWithdrawal: start with withdrawalId {}", withdrawalId);

        try {
            ProcessResult processResult = withdrawalHandler.handler(withdrawal, state, options);
            log.info("processWithdrawal: finish {} with withdrawalId {}", processResult, withdrawalId);
            return processResult;
        } catch (Exception ex) {
            String message = "Exception in processPayment with withdrawalId " + withdrawalId;

            if (isUndefinedResultOrUnavailable(ex)) {
                log.warn(message, ex);
            } else {
                log.error(message, ex);
            }

            throw ex;
        }
    }
}
