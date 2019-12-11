package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyWrapper;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper.*;
import static org.junit.Assert.assertEquals;

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
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore("Integration test")
public class MocketBankServerHandlerRecurrentSuccessIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandlerRecurrentSuccessIntegrationTest.class);

    @ClassRule
    public final static IntegrationBaseRule rule = new IntegrationBaseRule();

    @Autowired
    private MocketBankServerHandler handler;

    @Autowired
    protected com.rbkmoney.damsel.cds.StorageSrv.Iface cds;

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
    public void testProcessPaymentSuccess() throws TException, IOException, URISyntaxException {
        String[] cards = {
                "4242424242424242",
                "5555555555554444",
                "586824160825533338",
        };

        // Put the card and save the response to a subsequent request
        for (String card : cards) {
            CardData cardData = CdsWrapper.makeCardDataWithExpDate(
                    "NONAME",
                    "123",
                    card,
                    Byte.parseByte("12"),
                    Short.parseShort("2020")
            );
            processPaymentSuccess(cardData);
        }

    }

    private void processPaymentSuccess(CardData cardData) throws TException, URISyntaxException, IOException {
        PutCardDataResult putCardDataResponse = cdsPutCardData(cardData);

        RecurrentTokenContext context = new RecurrentTokenContext();
        context.setSession(new RecurrentTokenSession());
        context.setTokenInfo(
                makeRecurrentTokenInfo(
                        makeRecurrentPaymentTool(
                                makeDisposablePaymentResource(
                                        putCardDataResponse.getSessionId(),
                                        DomainWrapper.makePaymentTool(
                                                putCardDataResponse.getBankCard()
                                        )
                                )
                        )
                )
        );

        RecurrentTokenProxyResult generationProxyResult = handler.generateToken(context);

        String token = generationProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
        PaymentProxyResult processResultPayment = handler.processPayment(
                getContext(
                        getPaymentResourceRecurrent(token),
                        ProxyProviderWrapper.makeTargetProcessed(),
                        null
                )
        );

        assertEquals("Process payment ", ProxyWrapper.makeFinishStatusSuccess(), processResultPayment.getIntent().getFinish().getStatus());

        if (processResultPayment.getIntent().getFinish().getStatus().equals(ProxyWrapper.makeFinishStatusSuccess())) {

            LOGGER.info("Call capture payment");
            // Обрабатываем ответ и вызываем CapturePayment
            PaymentProxyResult processResultCapture = handler.processPayment(
                    getContext(
                            getPaymentResourceRecurrent(token),
                            ProxyProviderWrapper.makeTargetCaptured(),
                            DomainWrapper.makeTransactionInfo(
                                    processResultPayment.getTrx().getId(),
                                    Collections.emptyMap()
                            )
                    )
            );

            assertEquals("Process Capture ", ProxyWrapper.makeFinishStatusSuccess(), processResultCapture.getIntent().getFinish().getStatus());

            // Обрабатываем ответ
            LOGGER.info("Response capture payment {}", processResultCapture.toString());
        }
    }

    private Map<String, String> getOptionsProxy() {
        Map<String, String> options = new HashMap<>();
        options.put("merchantId", merchantId);
        options.put("merchantName", merchantName);
        options.put("merchantUrl", merchantUrl);
        options.put("merchantAcquirerBin", merchantAcquirerBin);
        options.put("merchantPassword", merchantPassword);
        options.put("merchantCountryCode", merchantCountryCode);
        return options;
    }

    private PaymentInfo getPaymentInfo(TransactionInfo transactionInfo, PaymentResource paymentResource) {
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
                        paymentResource,
                        getCost(),
                        transactionInfo,
                        Boolean.FALSE
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

    private PaymentResource getPaymentResourceRecurrent(String token) {
        return ProxyProviderWrapper.makePaymentResourceRecurrentPaymentResource(
                ProxyProviderWrapper.makeRecurrentPaymentResource(token)
        );
    }

    private byte[] getSessionState() throws IOException {
        return Converter.mapToByteArray(Collections.emptyMap());
    }

    private PaymentContext getContext(PaymentResource paymentResource, TargetInvoicePaymentStatus target, TransactionInfo transactionInfo) throws IOException {
        return ProxyProviderWrapper.makeContext(
                getPaymentInfo(transactionInfo, paymentResource),
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

    protected PutCardDataResult cdsPutCardData(CardData cardData) throws TException {
        LOGGER.info("CDS: put card request start");

        Auth3DS auth3DS = CdsWrapper.makeAuth3DS("jKfi3B417+zcCBFYbFp3CBUAAAA=", "5");
        AuthData authData = CdsWrapper.makeAuthDataWithAuth3DS(auth3DS);

        SessionData sessionData = CdsWrapper.makeSessionData(authData);

        PutCardDataResult putCardDataResponse = cds.putCardData(cardData, sessionData);
        putCardDataResponse.getBankCard().setExpDate(TestData.makeBankCard().getExpDate());
        LOGGER.info("CDS: put card response {}", putCardDataResponse);
        return putCardDataResponse;
    }

}
