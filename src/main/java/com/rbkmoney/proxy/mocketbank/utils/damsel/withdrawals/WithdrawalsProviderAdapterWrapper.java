package com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.FinishStatus;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;

public class WithdrawalsProviderAdapterWrapper {

    // ProxyResult
    public static ProcessResult makeProcessResult(
            com.rbkmoney.damsel.withdrawals.provider_adapter.Intent intent,
            Value nextState
    ) {
        ProcessResult processResult = new ProcessResult();
        processResult.setIntent(intent);
        processResult.setNextState(nextState);
        return processResult;
    }

    public static ProcessResult makeProcessResult(com.rbkmoney.damsel.withdrawals.provider_adapter.Intent intent) {
        return WithdrawalsProviderAdapterWrapper.makeProcessResult(intent, null);
    }

    // FinishIntent
    public static com.rbkmoney.damsel.withdrawals.provider_adapter.Intent makeFinishIntentSuccess(TransactionInfo transactionInfo) {
        com.rbkmoney.damsel.withdrawals.provider_adapter.FinishIntent finishIntent = new com.rbkmoney.damsel.withdrawals.provider_adapter.FinishIntent();
        finishIntent.setStatus(makeFinishStatusSuccess(transactionInfo));
        return com.rbkmoney.damsel.withdrawals.provider_adapter.Intent.finish(finishIntent);
    }

    public static FinishStatus makeFinishStatusSuccess(TransactionInfo transactionInfo) {
        return FinishStatus.success(
                new com.rbkmoney.damsel.withdrawals.provider_adapter.Success(
                        transactionInfo
                )
        );
    }

    public static ProcessResult makeProcessResultFailure(Failure failure) {
        com.rbkmoney.damsel.withdrawals.provider_adapter.FinishIntent finishIntent = new com.rbkmoney.damsel.withdrawals.provider_adapter.FinishIntent();
        finishIntent.setStatus(makeFinishStatusFailure(failure));

        com.rbkmoney.damsel.withdrawals.provider_adapter.Intent intent = new com.rbkmoney.damsel.withdrawals.provider_adapter.Intent();
        intent.setFinish(finishIntent);
        return makeProcessResult(intent);
    }


    public static ProcessResult makeProcessResultFailure(ErrorMapping errorMapping, String code, String description) {
        Failure failure = errorMapping.getFailureByCodeAndDescription(code, description);
        return makeProcessResultFailure(failure);
    }

    public static FinishStatus makeFinishStatusFailure(Failure failure) {
        return FinishStatus.failure(failure);
    }

}
