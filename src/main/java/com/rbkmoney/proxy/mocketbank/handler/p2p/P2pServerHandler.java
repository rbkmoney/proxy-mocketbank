package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.proxy.mocketbank.creator.P2pAdapterCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;

@Slf4j
@Component
public class P2pServerHandler implements P2PAdapterSrv.Iface {

    @Override
    public ProcessResult process(Context context) throws TException {
        String transactionId = P2pAdapterCreator.createTransactionId(context);
        return new ProcessResult()
                .setIntent(P2pAdapterCreator.createFinishIntentSuccess())
                .setTrx(createTransactionInfo(transactionId, Collections.emptyMap()));
    }

    @Override
    public CallbackResult handleCallback(Callback callback, Context context) throws TException {
        throw new UnsupportedOperationException("P2pHandler handleCallback not supported!");
    }
}
