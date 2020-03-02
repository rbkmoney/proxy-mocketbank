package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.proxy.mocketbank.TestData;
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
        TestCard[] cards = {
                Visa.INSUFFICIENT_FUNDS,
                Mastercard.INSUFFICIENT_FUNDS,
                Visa.INVALID_CARD,
                Mastercard.INVALID_CARD,
                Visa.CVV_MATCH_FAIL,
                Mastercard.CVV_MATCH_FAIL,
                Visa.EXPIRED,
                Mastercard.EXPIRED,
                Visa.UNKNOWN_FAILURE,
                Mastercard.UNKNOWN_FAILURE
        };

        for (TestCard card : cards) {
            CardData cardData = createCardData(card.getCardNumber());
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
