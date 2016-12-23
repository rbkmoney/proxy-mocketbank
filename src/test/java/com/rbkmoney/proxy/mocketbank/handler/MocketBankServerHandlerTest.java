package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.proxy_provider.CallbackResult;
import com.rbkmoney.damsel.proxy_provider.Context;
import com.rbkmoney.damsel.proxy_provider.ProxyResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;


public class MocketBankServerHandlerTest {

    @Mock
    ProxyResult proxyResult;

    @Mock
    CallbackResult callbackResult;

    @Mock
    ByteBuffer byteBuffer;

    @Mock
    Context context;

    @Mock
    MocketBankServerHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAuthPayment() throws Exception {
        Mockito.when(handler.processPayment(context)).thenReturn(proxyResult);
        assertEquals(proxyResult, handler.processPayment(context));
    }

    @Test
    public void testHandleAuthCallback() throws Exception {
        Mockito.when(handler.handlePaymentCallback(byteBuffer, context)).thenReturn(callbackResult);
        assertEquals(callbackResult, handler.handlePaymentCallback(byteBuffer, context));
    }

}
