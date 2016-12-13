package com.rbkmoney.proxy.test.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.UnlockStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.test.utils.Converter;
import com.rbkmoney.proxy.test.utils.cds.CdsApi;
import com.rbkmoney.proxy.test.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.test.utils.damsel.DomainWrapper;
import com.rbkmoney.proxy.test.utils.damsel.ProxyProviderWrapper;
import com.rbkmoney.proxy.test.utils.damsel.ProxyWrapper;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        }
)
//@Ignore("Integration test")
public class TestServerHandlerSuccessWith3DSIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestServerHandlerSuccessWith3DSIntegrationTest.class);

    @Autowired
    private TestServerHandler handler;

    @Autowired
    private CdsApi cds;

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
            "4012888888881881",
//            "5169147129584558",
        };

        // Put the card and save the response to a subsequent request
        for (String card: cards) {
            CardData cardData = CdsWrapper.makeCardDataWithExpDate(
                    "NONAME", "123", card, Byte.parseByte("12"), Short.parseShort("2020")
            );
            processPaymentSuccess(cardData);
        }

    }

    private void processPaymentSuccess(CardData cardData) throws TException, URISyntaxException, IOException {
        PutCardDataResult putCardDataResponse = cdsPutCardData(cardData);

        ProxyResult processResultPayment = handler.processPayment(
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
        CallbackResult callbackResult = handler.handlePaymentCallback(
                callbackMap, getContext(putCardDataResponse, null, null)
        );

        assertTrue("CallbackResult ", callbackResult.getResult().getIntent().getFinish().getStatus().isSetOk());

        LOGGER.info("Call capture payment");
        // Обрабатываем ответ и вызываем CapturePayment
        ProxyResult processResultCapture = handler.processPayment(
                getContext(
                        putCardDataResponse,
                        ProxyProviderWrapper.makeTargetCaptured(),
                        DomainWrapper.makeTransactionInfo(
                                callbackResult.getResult().getTrx().getId(),
                                Collections.emptyMap()
                        )
                )
        );

        assertEquals("Process Capture ", ProxyWrapper.makeFinishStatusOk(), processResultCapture.getIntent().getFinish().getStatus());

        // Обрабатываем ответ
        LOGGER.info("Response capture payment {}", processResultCapture.toString());
    }

    private Map<String, String> getOptionsProxy() {
        return Collections.emptyMap();
    }

    private PaymentInfo getPaymentInfo(PutCardDataResult putCardDataResponse, TransactionInfo transactionInfo) {
        return ProxyProviderWrapper.makePaymentInfo(
                ProxyProviderWrapper.makeInvoice(
                        invoiceId,
                        "2016-06-02",
                        "product",
                        getCost(),
                        "Invoice description"
                ),
                ProxyProviderWrapper.makeShop(
                        "shopId",
                        DomainWrapper.makeCategory("CategoryName", "Category description"),
                        DomainWrapper.makeShopDetails("ShopName", "Shop description")
                ),
                ProxyProviderWrapper.makeInvoicePaymentWithTrX(
                        paymentId,
                        "2016-06-02",
                        DomainWrapper.makePayer(
                                DomainWrapper.makeContactInfo("email", "phone"),
                                DomainWrapper.makeClientInfo("fingerprint", "ip"),
                                DomainWrapper.makePaymentTool(putCardDataResponse.getBankCard()),
                                putCardDataResponse.getSession()
                        ),
                        getCost(),
                        transactionInfo
                )
        );
    }

    private byte[] getSessionState() throws IOException {
        Map<String, String> extra = new HashMap<>();
        extra.put("paReq","paReq");
        return Converter.mapToByteArray(extra);
    }

    private Context getContext(PutCardDataResult putCardDataResult, Target target, TransactionInfo transactionInfo) throws IOException {
        return ProxyProviderWrapper.makeContext(
                getPaymentInfo(putCardDataResult, transactionInfo),
                ProxyProviderWrapper.makeSession(
                        target,
                        getSessionState()
                ),
                getOptionsProxy()
        );
    }


    private Cash getCost() {
        return ProxyProviderWrapper.makeCash(
                ProxyProviderWrapper.makeCurrency("Rubles", (short) 643, "RUB", (short) 2),
                10000L
        );
    }

    private PutCardDataResult cdsPutCardData(CardData cardData) throws TException, URISyntaxException, IOException {
        LOGGER.info("CDS: put card");
        PutCardDataResult putCardDataResponse = cds.putCardData(cardData);

        LOGGER.info(putCardDataResponse.toString());
        return putCardDataResponse;
    }

    /**
     * CDS unlock Key
     * <p>
     * TODO: Игнорируем, если ключи уже разблокированы
     *
     * @param thres_hold short
     * @param num_shares short
     * @return UnlockStatus
     * @throws URISyntaxException
     * @throws TException
     */
    private UnlockStatus cdsUnlockKey(short thres_hold, short num_shares) throws URISyntaxException, TException, IOException {
        LOGGER.info("CDS: init");
        List<ByteBuffer> key = cds.init(thres_hold, num_shares);
        LOGGER.info("CDS: init response {}", key.toString());

        LOGGER.info("CDS: unlock");
        UnlockStatus unlockStatus = cds.unlock(key.get(0));
        LOGGER.info("CDS: unlock res {}", unlockStatus.toString());
        return unlockStatus;
    }

}
