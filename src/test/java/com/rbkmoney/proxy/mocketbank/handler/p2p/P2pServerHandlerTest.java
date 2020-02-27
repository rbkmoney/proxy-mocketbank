package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.p2p_adapter.Callback;
import com.rbkmoney.damsel.p2p_adapter.Context;
import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.utils.p2p.creator.P2pCreator;
import com.rbkmoney.proxy.mocketbank.utils.p2p.verification.P2pVerification;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class P2pServerHandlerTest {

    @Autowired
    private P2pServerHandler handler;

    @Test
    public void testProcess() throws TException {
        Context context = P2pCreator.createContext();
        ProcessResult result = handler.process(context);
        assertTrue(P2pVerification.isSuccess(result));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testHandleCallback() throws TException {
        Context context = P2pCreator.createContext();
        Callback callback = P2pCreator.createCallback();
        handler.handleCallback(callback, context);
    }

}