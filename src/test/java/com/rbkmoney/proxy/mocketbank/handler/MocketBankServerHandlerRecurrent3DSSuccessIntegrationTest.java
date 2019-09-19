package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyWrapper;
import org.apache.thrift.TException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.proxy.mocketbank.utils.Converter.byteArrayToMap;
import static com.rbkmoney.proxy.mocketbank.utils.Converter.mapToByteBuffer;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper.makeCurrency;
import static com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper.*;
import static org.apache.commons.lang3.StringUtils.trim;
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
public class MocketBankServerHandlerRecurrent3DSSuccessIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandlerRecurrent3DSSuccessIntegrationTest.class);

    @ClassRule
    public final static IntegrationBaseRule rule = new IntegrationBaseRule();

    protected String recurrentId = "Recurrent" + (int) (Math.random() * 500 + 1);

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
                "4012888888881881",
                "5169147129584558",
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
                                recurrentId,
                                makeDisposablePaymentResource(
                                        putCardDataResponse.getSessionId(),
                                        DomainWrapper.makePaymentTool(
                                                putCardDataResponse.getBankCard()
                                        )
                                ),
                                makeCash(
                                        makeCurrency(
                                                "Rubles",
                                                (short) 643,
                                                "RUB",
                                                (short) 1
                                        ),
                                        1000L
                                )
                        )
                )
        );

        LOGGER.info("Prepare RecurrentTokenContext - finish");
        // ------------------------------------------------------------------------
        // generate Token
        // ------------------------------------------------------------------------
        RecurrentTokenProxyResult generationProxyResult = handler.generateToken(context);
        LOGGER.info("Response generate Token {}", generationProxyResult);


        // ------------------------------------------------------------------------
        // ACS PAGE
        // ------------------------------------------------------------------------
        Map<String, String> extraParams = byteArrayToMap(generationProxyResult.getNextState());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MD", trim(extraParams.get("MD")));
        params.add("TermUrl", trim(extraParams.get("TermUrl")));
        params.add("PaReq", trim(extraParams.get("PaReq")));

        HttpEntity<?> entity = new HttpEntity<>(params, headers);
        String acsUrl = generationProxyResult.getIntent().getSuspend().getUserInteraction().getRedirect().getPostRequest().getUri();

        ResponseEntity responseThreeDS = new RestTemplate().exchange(acsUrl, HttpMethod.POST, entity, String.class);

        Document doc = Jsoup.parse(responseThreeDS.getBody().toString());
        Elements elements = doc.getElementsByTag("input");

        HashMap<String, String> threeDSparams = new HashMap<>();
        elements.forEach(
                element -> {
                    if (!element.attributes().get("name").isEmpty()) {
                        threeDSparams.put(element.attributes().get("name"), trim(element.attributes().get("value")));
                    }
                }
        );


        // ------------------------------------------------------------------------
        // handleRecurrentTokenCallback
        // ------------------------------------------------------------------------
        ByteBuffer callback = mapToByteBuffer(threeDSparams);

        RecurrentTokenSession recurrentTokenSession = new RecurrentTokenSession();
        context.setSession(recurrentTokenSession);

        context.getSession().setState(generationProxyResult.getNextState());

        RecurrentTokenCallbackResult recurrentTokenCallback = handler.handleRecurrentTokenCallback(callback, context);
        LOGGER.info("Response handleRecurrentTokenCallback {}", recurrentTokenCallback);

        assertEquals("recurrentTokenCallback ", true, recurrentTokenCallback.getResult().getIntent().getFinish().getStatus().isSetSuccess());

        PaymentResource paymentResource = new PaymentResource();
        RecurrentPaymentResource recurrentPaymentResource = new RecurrentPaymentResource();
        recurrentPaymentResource.setRecToken(recurrentTokenCallback.getResult().getIntent().getFinish().getStatus().getSuccess().getToken());
        recurrentPaymentResource.setPaymentTool(DomainWrapper.makePaymentTool(
                putCardDataResponse.getBankCard()
        ));
        paymentResource.setRecurrentPaymentResource(recurrentPaymentResource);

        PaymentInfo paymentInfo = getPaymentInfo(putCardDataResponse, null, paymentResource);
        PaymentContext contextProcessed = ProxyProviderWrapper.makeContext(
                paymentInfo,
                ProxyProviderWrapper.makeSession(
                        ProxyProviderWrapper.makeTargetProcessed(),
                        "init".getBytes()
                ),
                getOptionsProxy()
        );

        // ------------------------------------------------------------------------
        // processed payment
        // ------------------------------------------------------------------------

        PaymentProxyResult processResultPayment = handler.processPayment(contextProcessed);
        LOGGER.info("Response process payment {}", processResultPayment.toString());

        assertEquals("Process payment ", ProxyWrapper.makeFinishStatusSuccess(), processResultPayment.getIntent().getFinish().getStatus());


        if (processResultPayment.getIntent().getFinish().getStatus().equals(ProxyWrapper.makeFinishStatusSuccess())) {

            LOGGER.info("Update payment {} ", paymentInfo.getPayment());
            contextProcessed.getPaymentInfo().getPayment().setTrx(processResultPayment.getTrx());
            contextProcessed.getSession().setState("captured".getBytes());
            contextProcessed.getSession().setTarget(ProxyProviderWrapper.makeTargetCaptured());

            // ------------------------------------------------------------------------
            // captured payment
            // ------------------------------------------------------------------------

            PaymentProxyResult processResultCapture = handler.processPayment(contextProcessed);
            LOGGER.info("Response capture payment {}", processResultCapture);

            assertEquals("Process Capture ", ProxyWrapper.makeFinishStatusSuccess(), processResultCapture.getIntent().getFinish().getStatus());
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
        return makePaymentInfo(
                makeInvoice(
                        invoiceId,
                        "2016-06-02",
                        getCost()
                ),
                makeShop(
                        DomainWrapper.makeCategory("CategoryName", "Category description"),
                        DomainWrapper.makeShopDetails("ShopName", "Shop description")
                ),
                makeInvoicePaymentWithTrX(
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
        return makePaymentResourceDisposablePaymentResource(
                DomainWrapper.makeDisposablePaymentResource(
                        DomainWrapper.makeClientInfo("fingerprint", "ip"),
                        putCardDataResponse.getSessionId(),
                        DomainWrapper.makePaymentTool(putCardDataResponse.getBankCard())
                )
        );
    }

    private PaymentResource getPaymentResourceRecurrent(String token) {
        return makePaymentResourceRecurrentPaymentResource(
                makeRecurrentPaymentResource(token)
        );
    }

    private byte[] getSessionState() throws IOException {
        return Converter.mapToByteArray(Collections.emptyMap());
    }

    private PaymentContext getContext(PaymentResource paymentResource, TargetInvoicePaymentStatus target, TransactionInfo transactionInfo) throws IOException {
        return makeContext(
                getPaymentInfo(transactionInfo, paymentResource),
                makeSession(
                        target,
                        getSessionState()
                ),
                getOptionsProxy()
        );
    }


    private Cash getCost() {
        return makeCash(
                makeCurrency("Rubles", (short) 643, "RUB", (short) 2),
                10000L
        );
    }

    protected PutCardDataResult cdsPutCardData(CardData cardData) throws TException {
        LOGGER.info("CDS: put card request start");

        Auth3DS auth3DS = CdsWrapper.makeAuth3DS("jKfi3B417+zcCBFYbFp3CBUAAAA=", "5");
        AuthData authData = CdsWrapper.makeAuthDataWithAuth3DS(auth3DS);

        SessionData sessionData = CdsWrapper.makeSessionData(authData);

        PutCardDataResult putCardDataResponse = cds.putCardData(cardData, sessionData);
        LOGGER.info("CDS: put card response {}", putCardDataResponse);
        return putCardDataResponse;
    }


    protected PaymentInfo getPaymentInfo(PutCardDataResult putCardDataResponse, TransactionInfo transactionInfo, PaymentResource paymentResource) {
        Invoice invoice = ProxyProviderWrapper.makeInvoice(
                invoiceId,
                "2016-06-02",
                ProxyProviderWrapper.makeCash(
                        ProxyProviderWrapper.makeCurrency(
                                "Rubles",
                                (short) 643,
                                "RUB",
                                (short) 1
                        ),
                        1000L
                )
        );


        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setInvoice(invoice);

        InvoicePayment payment = getInvoicePayment(
                putCardDataResponse,
                paymentResource,
                DomainWrapper.makePaymentTool(
                        putCardDataResponse.getBankCard()
                ),
                transactionInfo
        );

        paymentInfo.setPayment(payment);
        return paymentInfo;
    }

    protected InvoicePayment getInvoicePayment(PutCardDataResult putCardDataResponse, PaymentResource paymentResource, PaymentTool paymentTool, TransactionInfo transactionInfo) {
        InvoicePayment payment = new InvoicePayment();
        payment.setId(paymentId);
        payment.setCreatedAt("2016-06-02");
        payment.setPaymentResource(paymentResource);
        payment.setCost(
                ProxyProviderWrapper.makeCash(
                        ProxyProviderWrapper.makeCurrency(
                                "Rubles",
                                (short) 643,
                                "RUB",
                                (short) 1
                        ),
                        1000L
                )
        );
        payment.setTrx(transactionInfo);
        return payment;
    }
}
