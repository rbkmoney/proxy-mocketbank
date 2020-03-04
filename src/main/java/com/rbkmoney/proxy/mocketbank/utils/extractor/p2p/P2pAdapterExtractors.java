package com.rbkmoney.proxy.mocketbank.utils.extractor.p2p;

import com.rbkmoney.damsel.p2p_adapter.Context;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class P2pAdapterExtractors {

    public static String extractOperationId(Context context) {
        return context.getOperation().getProcess().getId();
    }

    public static String extractSessionId(Context context) {
        return context.getSession().getId();
    }

}
