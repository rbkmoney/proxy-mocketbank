package com.rbkmoney.proxy.mocketbank.utils.cds;

import com.rbkmoney.damsel.base.Ok;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.UnlockStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CdsApiTest {

    @Mock
    private CdsApi cds;

    @Mock
    private Ok ok;

    @Mock
    private CardData cardData;

    @Mock
    private PutCardDataResult putCardDataResult;

    @Before
    public void setUp() throws Exception {
        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under test.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUnlock() throws Exception {
        ByteBuffer bbuf = ByteBuffer.allocate(10);
        UnlockStatus status = UnlockStatus.ok(ok);

        Mockito.when(cds.unlock(bbuf)).thenReturn(status);

        assertEquals(status, cds.unlock(bbuf));
    }

    @Test
    public void testInit() throws Exception {
        short threshold = 1;
        short num_shares = 1;

        List<ByteBuffer> list = new LinkedList<>();
        ByteBuffer bbuf = ByteBuffer.allocate(10);
        list.add(bbuf);

        Mockito.when(cds.init(threshold, num_shares)).thenReturn(list);

        assertEquals(list, cds.init(threshold, num_shares));
    }

}
