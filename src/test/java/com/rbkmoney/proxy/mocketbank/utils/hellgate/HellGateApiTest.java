package com.rbkmoney.proxy.mocketbank.utils.hellgate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class HellGateApiTest {

    private ByteBuffer bbuf = ByteBuffer.wrap("some_byte".getBytes());
    private ByteBuffer response = ByteBuffer.wrap("some_response_byte".getBytes());

    @Mock
    private HellGateApi hellGate;

    @Before
    public void setUp() throws Exception {
        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under test.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessCallback() throws Exception {
        String tag = "common_tag";
        Mockito.when(hellGate.processPaymentCallback(tag, bbuf)).thenReturn(response);
        assertEquals(response, hellGate.processPaymentCallback(tag, bbuf));
    }

    @Test
    public void testProcessRecurrentTokenCallback() throws Exception {
        String tag = "recurrent_tag";
        Mockito.when(hellGate.processRecurrentTokenCallback(tag, bbuf)).thenReturn(response);
        assertEquals(response, hellGate.processRecurrentTokenCallback(tag, bbuf));
    }

}
