package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.UnlockStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.Context;
import com.rbkmoney.damsel.proxy_provider.PaymentInfo;
import com.rbkmoney.damsel.proxy_provider.ProxyResult;
import com.rbkmoney.damsel.proxy_provider.TargetInvoicePaymentStatus;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore("Integration test")
public class MocketBankServerHandlerFailIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandlerFailIntegrationTest.class);

    @Autowired
    private MocketBankServerHandler handler;

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
            "4000000000000002",
            "5100000000000412",
            "4222222222222220",
            "5100000000000511",
            "4003830171874018",
            "5496198584584769",
            "4000000000000069",
            "5105105105105100",
            "4111110000000112",
            "5124990000000002",
        };

        // Put the card and save the response to a subsequent request
        for (String card: cards) {
            CardData cardData = CdsWrapper.makeCardDataWithExpDate(
                    "NONAME",
                    "123",
                    card,
                    Byte.parseByte("12"),
                    Short.parseShort("2020")
            );
            processPaymentFail(cardData);
        }

    }

    private void processPaymentFail(CardData cardData) throws TException, URISyntaxException, IOException {
        PutCardDataResult putCardDataResponse = cdsPutCardData(cardData);

        ProxyResult processResultPayment = handler.processPayment(
                getContext(
                        putCardDataResponse,
                        ProxyProviderWrapper.makeTargetProcessed(),
                        null
                )
        );

        assertTrue("Process payment ", processResultPayment.getIntent().getFinish().getStatus().getFailure().isSetCode());
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
        return Converter.mapToByteArray(Collections.emptyMap());
    }

    private Context getContext(PutCardDataResult putCardDataResult, TargetInvoicePaymentStatus target, TransactionInfo transactionInfo) throws IOException {
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
