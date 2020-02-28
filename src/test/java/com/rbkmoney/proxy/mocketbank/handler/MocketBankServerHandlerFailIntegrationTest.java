package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isFailure;
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
public class MocketBankServerHandlerFailIntegrationTest extends IntegrationTest {

    @Test
    public void testProcessPaymentFail() throws TException {
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

        for (String card : cards) {
            CardData cardData = createCardData(card);
            processPaymentFail(cardData);
        }
    }

    private void processPaymentFail(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);

        PaymentProxyResult processResultPayment = handler.processPayment(getContext(bankCard, createTargetProcessed(), null));
        assertTrue("Process payment isn`t failure", isFailure(processResultPayment));
    }

}
