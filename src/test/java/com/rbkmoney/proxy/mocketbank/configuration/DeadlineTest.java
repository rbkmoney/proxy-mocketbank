package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.ProviderProxySrv;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenSession;
import com.rbkmoney.damsel.proxy_provider.Shop;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.flow.error.WUnavailableResultException;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createDisposablePaymentResource;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.woody.api.flow.error.WErrorType.UNAVAILABLE_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "restTemplate.networkTimeout=10000",
                "server.port=7021",
                "server.rest.port=7022",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:${server.port}"
        }
)
public class DeadlineTest {

    public static int SLEEP_FOR_BANK_CONTROLLER = 5000;

    private static final int TIMEOUT = 5000;
    private static final String token = "card_token";

    @LocalServerPort
    private int port;

    @Autowired
    protected List<Card> cardList;

    @MockBean
    private CdsClientStorage cds;

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
            assertEquals(UNAVAILABLE_RESULT, ex.getErrorDefinition().getErrorType());
        }
    }

    @Test(expected = WUnavailableResultException.class)
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
        Mockito.when(cds.getCardData((RecurrentTokenContext) any())).thenReturn(TestData.createCardDataProxyModel(extractSuccess3dsCard()));
    }

    private void deadlineTest() throws TException {
        RecurrentTokenContext context = new RecurrentTokenContext();
        context.setSession(new RecurrentTokenSession());
        context.setTokenInfo(
                createRecurrentTokenInfo(
                        createRecurrentPaymentTool(
                                "Recurrent" + (int) (Math.random() * 500 + 1),
                                createDisposablePaymentResource(
                                        "session_id",
                                        createPaymentTool(
                                                new BankCard(token, BankCardPaymentSystem.visa, "bin", "masked_pan").setExpDate(createBankCardExpDate("12", "2020"))
                                        )
                                ),
                                createCash(
                                        createCurrency("Rubles", (short) 643, "RUB", (short) 1),
                                        1000L
                                )
                        )
                ).setShop(prepareShop())
        );
        client.generateToken(context);
    }

    private Shop prepareShop() {
        ShopLocation shopLocation = new ShopLocation();
        shopLocation.setUrl("url");
        return new Shop()
                .setId("shop_id")
                .setCategory(new Category().setName("CategoryName").setDescription("Category description"))
                .setDetails(new ShopDetails().setName("ShopName").setDescription("Shop description"))
                .setLocation(shopLocation);
    }

    private String extractSuccess3dsCard() {
        String[] pans = cardList.stream()
                .filter(CardAction::isMpiCardSuccess)
                .map(Card::getPan)
                .toArray(String[]::new);
        return pans[0];
    }
}
