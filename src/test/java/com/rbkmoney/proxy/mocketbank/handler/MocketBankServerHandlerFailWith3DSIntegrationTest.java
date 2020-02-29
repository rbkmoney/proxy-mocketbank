package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackResult;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiEnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiTransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards.Mastercard;
import com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards.TestCard;
import com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards.Visa;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuspend;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:8018",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerFailWith3DSIntegrationTest extends IntegrationTest {

    @Test
    public void testProcessPaymentFail() throws TException, IOException {
        TestCard[] cards = {
                Visa.FAILURE_3DS,
                Mastercard.FAILURE_3DS,
                Visa.TIMEOUT_3DS,
                Mastercard.TIMEOUT_3DS
        };

        for (TestCard card : cards) {
            CardData cardData = createCardData(card.getCardNumber());
            processPaymentFail(cardData);
        }
    }

    private void processPaymentFail(CardData cardData) throws TException, IOException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);
        mockMpiVerify(MpiEnrollmentStatus.AUTHENTICATION_AVAILABLE);
        mockMpi(MpiTransactionStatus.AUTHENTICATION_FAILED);

        PaymentContext paymentContext = getContext(bankCard, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process payment isn`t suspend", isSuspend(proxyResult));

        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put("MD", "MD-TAG");
        mapCallback.put("paRes", "SomePaRes");
        paymentContext.getSession().setState(proxyResult.getNextState());
        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        PaymentCallbackResult callbackResult = handler.handlePaymentCallback(callbackMap, paymentContext);
        assertTrue("CallbackResult isn`t failure", isCallbackFailure(callbackResult));
    }

}
