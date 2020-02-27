package com.rbkmoney.proxy.mocketbank.utils.p2p.verification;

import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class P2pVerification {

    public static boolean isSuccess(ProcessResult processResult) {
        return processResult.getIntent().getFinish().getStatus().isSetSuccess();
    }

}
