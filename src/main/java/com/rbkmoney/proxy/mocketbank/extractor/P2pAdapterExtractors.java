package com.rbkmoney.proxy.mocketbank.extractor;

import com.rbkmoney.damsel.p2p_adapter.Context;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class P2pAdapterExtractors {

    public static String extractOperationId(Context context) {
        return context.getOperation().getProcess().getId();
    }

    public static String extractSessionId(Context context) {
        return context.getSession().getId();
    }

}
