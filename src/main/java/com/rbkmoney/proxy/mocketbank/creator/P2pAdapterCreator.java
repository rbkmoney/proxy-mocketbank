package com.rbkmoney.proxy.mocketbank.creator;

import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.proxy.mocketbank.extractor.P2pAdapterExtractors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class P2pAdapterCreator {

    public static final String DELIMITER = ".";

    public static String createTransactionId(Context context) {
        String operationId = P2pAdapterExtractors.extractOperationId(context);
        String sessionId = P2pAdapterExtractors.extractSessionId(context);
        return sessionId + DELIMITER + operationId;
    }

    public static Intent createFinishIntentSuccess() {
        FinishIntent finishIntent = new FinishIntent().setStatus(FinishStatus.success(new Success()));
        return Intent.finish(finishIntent);
    }

}
