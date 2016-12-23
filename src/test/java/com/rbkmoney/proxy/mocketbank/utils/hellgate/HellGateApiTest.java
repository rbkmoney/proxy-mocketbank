package com.rbkmoney.proxy.mocketbank.utils.hellgate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class HellGateApiTest {

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
        ByteBuffer bbuf = ByteBuffer.wrap("some_byte".getBytes());
        ByteBuffer response = ByteBuffer.wrap("some_response_byte".getBytes());

        String tag = "some_tag";

        Mockito.when(hellGate.processCallback(tag, bbuf)).thenReturn(response);

        assertEquals(response, hellGate.processCallback(tag, bbuf));
    }

}
