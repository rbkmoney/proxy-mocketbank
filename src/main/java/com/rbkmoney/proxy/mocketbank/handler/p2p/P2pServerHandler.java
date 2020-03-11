package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.proxy.mocketbank.handler.p2p.callback.P2pCallbackHandler;
import com.rbkmoney.proxy.mocketbank.handler.p2p.payment.ProcessedP2pHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class P2pServerHandler implements P2PAdapterSrv.Iface {

    private final P2pCallbackHandler p2pCallbackHandler;
    private final ProcessedP2pHandler processedP2pHandler;

    @Override
    public ProcessResult process(Context context) throws TException {
        return processedP2pHandler.process(context);
    }

    @Override
    public CallbackResult handleCallback(Callback callback, Context context) throws TException {
        return p2pCallbackHandler.handleCallback(callback, context);
    }
}
