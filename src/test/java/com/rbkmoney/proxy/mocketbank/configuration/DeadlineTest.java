package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.BankCardPaymentSystem;
import com.rbkmoney.damsel.proxy_provider.ProviderProxySrv;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenSession;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsStorageApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper.makeCurrency;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper.*;
import static com.rbkmoney.woody.api.flow.error.WErrorType.UNEXPECTED_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "restTemplate.networkTimeout=10000",
                "server.port=7021",
                "server.secondary.ports=7022",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:${server.port}"
        }
)
public class DeadlineTest {

    public static int SLEEP_FOR_BANK_CONTROLLER = 5000;

    private static final int TIMEOUT = 5000;
    private static final String token = "card_token";

    @LocalServerPort
    private int port;

    @MockBean
    private CdsStorageApi cds;

    private ProviderProxySrv.Iface client;

    @Before
    public void setup() throws Exception {
        createThriftClient();
        mockCdsBean();
    }

    @Test
    public void lessThenDeadlineTest() throws TException {
        try {
            SLEEP_FOR_BANK_CONTROLLER = TIMEOUT - 2000;
            deadlineTest();
            fail();
        } catch (WRuntimeException ex) {
            assertEquals(UNEXPECTED_ERROR, ex.getErrorDefinition().getErrorType());
        }
    }

    @Test(expected = WUndefinedResultException.class)
    public void moreThenDeadlineTest() throws TException {
        SLEEP_FOR_BANK_CONTROLLER = TIMEOUT + 2000;
        deadlineTest();
    }

    private void createThriftClient() throws URISyntaxException {
        client = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/proxy/mocketbank"))
                .withNetworkTimeout(TIMEOUT)
                .build(ProviderProxySrv.Iface.class);
    }

    private void mockCdsBean() {
        Mockito.when(cds.getCardData(token)).thenReturn(createCardData());
    }

    private CardData createCardData() {
        return CdsWrapper.makeCardDataWithExpDate(
                "NONAME",
                "123",
                "4012888888881881",
                Byte.parseByte("12"),
                Short.parseShort("2020")
        );
    }

    private void deadlineTest() throws TException {
        PutCardDataResult response = new PutCardDataResult(
                new BankCard(token, BankCardPaymentSystem.visa, "bin", "masked_pan"),
                "session_id"
        );
        RecurrentTokenContext context = new RecurrentTokenContext();
        context.setSession(new RecurrentTokenSession());
        context.setTokenInfo(
                makeRecurrentTokenInfo(
                        makeRecurrentPaymentTool(
                                "Recurrent" + (int) (Math.random() * 500 + 1),
                                makeDisposablePaymentResource(
                                        response.getSessionId(),
                                        DomainWrapper.makePaymentTool(response.getBankCard())
                                ),
                                makeCash(
                                        makeCurrency("Rubles", (short) 643, "RUB", (short) 1),
                                        1000L
                                )
                        )
                )
        );
        client.generateToken(context);
    }
}
