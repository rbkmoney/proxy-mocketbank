package com.rbkmoney.proxy.mocketbank.decorator;

import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.proxy.mocketbank.utils.extractor.p2p.P2pAdapterExtractors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

@Slf4j
@RequiredArgsConstructor
public class P2pServerHandlerLog implements P2PAdapterSrv.Iface {

    private final P2PAdapterSrv.Iface handler;

    @Override
    public ProcessResult process(Context context) throws TException {
        String sessionId = P2pAdapterExtractors.extractSessionId(context);
        log.info("Process started with sessionId={}", sessionId);
        try {
            ProcessResult result = handler.process(context);
            log.info("Process finished {} with sessionId={}", result, sessionId);
            return result;
        } catch (Exception ex) {
            String message = String.format("Failed handle Process with sessionId=%s", sessionId);
            ServerHandlerLogUtils.logMessage(ex, message, this.getClass());
            throw ex;
        }
    }

    @Override
    public CallbackResult handleCallback(Callback callback, Context context) throws TException {
        String sessionId = P2pAdapterExtractors.extractSessionId(context);
        log.info("HandleCallback started with sessionId={}", sessionId);
        try {
            CallbackResult result = handler.handleCallback(callback, context);
            log.info("HandleCallback finished {} with sessionId={}", result, sessionId);
            return result;
        } catch (Exception ex) {
            String message = String.format("Failed handle HandleCallback with sessionId=%s", sessionId);
            ServerHandlerLogUtils.logMessage(ex, message, this.getClass());
            throw ex;
        }
    }

}
