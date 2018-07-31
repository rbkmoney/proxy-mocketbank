package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.AdapterSrv;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal.WithdrawalHandler;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.utils.MocketBankUtils.isUndefinedResultOrUnavailable;

@Component
public class P2PCreditServerHandler  implements AdapterSrv.Iface  {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final WithdrawalHandler withdrawalHandler;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link P2PCreditServerHandler} instance with the given
     * initial parameters to be constructed.
     *
     * @param withdrawalHandler the field's withdrawalHandler (see {@link #withdrawalHandler}).
     */
    @Autowired
    public P2PCreditServerHandler(WithdrawalHandler withdrawalHandler) {
        this.withdrawalHandler = withdrawalHandler;
    }

    /**
     * Process Withdrawal
     *
     * @param withdrawal Data for request
     * @param state      Value some state
     * @param options    Map<String, String> Adapter options
     * @return ProcessResult
     * @throws TException exception
     */
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
