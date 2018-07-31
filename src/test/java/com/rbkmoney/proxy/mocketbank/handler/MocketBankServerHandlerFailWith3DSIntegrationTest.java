package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsStorageApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "merchant.id=24275801",
                "merchant.name=RBKmoney1",
                "merchant.url=http://localhost",
                "merchant.acquirerBin=422538",
                "merchant.password=",
                "merchant.countryCode=643",
                "cds.url.keyring=http://127.0.0.1:8021/v1/keyring",
                "cds.url.storage=http://127.0.0.1:8021/v1/storage",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:8018",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore("Integration test")
public class MocketBankServerHandlerFailWith3DSIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandlerFailWith3DSIntegrationTest.class);

    @ClassRule
    public final static IntegrationBaseRule rule = new IntegrationBaseRule();

    @Autowired
    private MocketBankServerHandler handler;

    @Autowired
    private CdsStorageApi cds;

    @Value("${merchant.id}")
    private String merchantId;

    @Value("${merchant.name}")
    private String merchantName;

    @Value("${merchant.url}")
    private String merchantUrl;

    @Value("${merchant.acquirerBin}")
    private String merchantAcquirerBin;

    @Value("${merchant.password}")
    private String merchantPassword;

    @Value("${merchant.countryCode}")
    private String merchantCountryCode;

    private String invoiceId = "TEST_INVOICE" + (int) (Math.random() * 50 + 1);
    private String paymentId = "TEST_PAYMENT" + (int) (Math.random() * 50 + 1);

    @Before
    public void setUp() {
        // Connect to CDS
        // TODO используется лишь в первый раз при запуске теста для разблокировки ключа
        // UnlockStatus unlockStatus = cdsUnlockKey((short) 1, (short) 1);
    }

    @Test
    public void testProcessPaymentFail() throws TException, IOException, URISyntaxException {
        String[] cards = {
            "4987654321098769",
            "5123456789012346",
            "4342561111111118",
            "5112000200000002",
        };

        // Put the card and save the response to a subsequent request
        for (String card: cards) {
            CardData cardData = CdsWrapper.makeCardDataWithExpDate(
                    "NONAME", "123", card, Byte.parseByte("12"), Short.parseShort("2020")
            );
            processPaymentFail(cardData);
        }

    }

    private void processPaymentFail(CardData cardData) throws TException, URISyntaxException, IOException {
        PutCardDataResult putCardDataResponse = cdsPutCardData(cardData);

        PaymentProxyResult processResultPayment = handler.processPayment(
                getContext(
                        putCardDataResponse,
                        ProxyProviderWrapper.makeTargetProcessed(),
                        null
                )
        );

        // Process Payment
        assertTrue("Process payment ", !processResultPayment.getIntent().getSuspend().getTag().isEmpty());


        // Prepare handlePaymentCallback
        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put("MD", "MD-TAG");
        mapCallback.put("paRes", "SomePaRes");

        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        // handlePaymentCallback
        PaymentCallbackResult callbackResult = handler.handlePaymentCallback(
                callbackMap, getContext(putCardDataResponse, null, null)
        );

        assertTrue("CallbackResult ", callbackResult.getResult().getIntent().getFinish().getStatus().getFailure().isSetCode());

    }

    private Map<String, String> getOptionsProxy() {
        return Collections.emptyMap();
    }

    private PaymentInfo getPaymentInfo(PutCardDataResult putCardDataResponse, TransactionInfo transactionInfo) {
        return ProxyProviderWrapper.makePaymentInfo(
                ProxyProviderWrapper.makeInvoice(
                        invoiceId,
                        "2016-06-02",
                        getCost()
                ),
                ProxyProviderWrapper.makeShop(
                        DomainWrapper.makeCategory("CategoryName", "Category description"),
                        DomainWrapper.makeShopDetails("ShopName", "Shop description")
                ),
                ProxyProviderWrapper.makeInvoicePaymentWithTrX(
                        paymentId,
                        "2016-06-02",
                        getPaymentResource(putCardDataResponse),
                        getCost(),
                        transactionInfo
                )
        );
    }

    private PaymentResource getPaymentResource(PutCardDataResult putCardDataResponse) {
        return ProxyProviderWrapper.makePaymentResourceDisposablePaymentResource(
                DomainWrapper.makeDisposablePaymentResource(
                        DomainWrapper.makeClientInfo("fingerprint", "ip"),
                        putCardDataResponse.getSessionId(),
                        DomainWrapper.makePaymentTool(putCardDataResponse.getBankCard())
                )
        );
    }

    private byte[] getSessionState() throws IOException {
        Map<String, String> extra = new HashMap<>();
        extra.put("paReq","paReq");
        return Converter.mapToByteArray(extra);
    }

    private PaymentContext getContext(PutCardDataResult putCardDataResult, TargetInvoicePaymentStatus target, TransactionInfo transactionInfo) throws IOException {
        return ProxyProviderWrapper.makeContext(
                getPaymentInfo(putCardDataResult, transactionInfo),
                ProxyProviderWrapper.makeSession(
                        target,
                        getSessionState()
                ),
                getOptionsProxy()
        );
    }


    private com.rbkmoney.damsel.proxy_provider.Cash getCost() {
        return ProxyProviderWrapper.makeCash(
                ProxyProviderWrapper.makeCurrency("Rubles", (short) 643, "RUB", (short) 2),
                10000L
        );
    }

    protected PutCardDataResult cdsPutCardData(CardData cardData) {
        LOGGER.info("CDS: put card request start");

        Auth3DS auth3DS = CdsWrapper.makeAuth3DS("jKfi3B417+zcCBFYbFp3CBUAAAA=", "5");
        AuthData authData = CdsWrapper.makeAuthData(auth3DS);

        SessionData sessionData = CdsWrapper.makeSessionData(authData);

        PutCardDataResult putCardDataResponse = cds.putCardData(cardData, sessionData);
        LOGGER.info("CDS: put card response {}", putCardDataResponse);
        return putCardDataResponse;
    }

}
