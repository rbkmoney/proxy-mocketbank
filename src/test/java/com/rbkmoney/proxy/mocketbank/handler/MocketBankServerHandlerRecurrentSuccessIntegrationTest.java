package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.p2p.constant.testcards.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createRecurrentPaymentTool;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createRecurrentTokenInfo;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerRecurrentSuccessIntegrationTest extends IntegrationTest {

    @Test
    public void testProcessPaymentSuccess() throws TException, IOException {
        TestCard[] cards = {
                Visa.SUCCESS,
                Mastercard.SUCCESS,
                Maestro.SUCCESS,
                Mir.SUCCESS
        };

        for (TestCard card : cards) {
            CardData cardData = createCardData(card.getCardNumber());
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        bankCard.setToken(TestData.BANK_CARD_TOKEN);
        mockCds(cardData, bankCard);

        RecurrentTokenContext context = new RecurrentTokenContext();
        context.setSession(new RecurrentTokenSession());
        context.setTokenInfo(
                createRecurrentTokenInfo(
                        createRecurrentPaymentTool(
                                createDisposablePaymentResource(
                                        createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                                        TestData.SESSION_ID,
                                        createPaymentTool(bankCard)
                                )
                        ).setId(recurrentId)
                )
        );

        RecurrentTokenProxyResult generationProxyResult = handler.generateToken(context);

        String token = generationProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();

        PaymentContext paymentContext = getContext(getPaymentResourceRecurrent(token), createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process payment isn`t success", isSuccess(proxyResult));

        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        paymentContext.getSession().setTarget(createTargetCaptured());

        PaymentProxyResult processResultCapture = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success ", isSuccess(processResultCapture));
    }

}
