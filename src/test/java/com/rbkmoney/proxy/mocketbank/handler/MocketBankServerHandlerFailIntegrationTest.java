package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.CardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isFailure;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerFailIntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentFail() throws TException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardFailed);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPaymentFail(cardData);
        }
    }

    private void processPaymentFail(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);

        PaymentProxyResult processResultPayment = handler.processPayment(
                getContext(bankCard, createTargetProcessed(), null));
        assertTrue("Process payment isn`t failure", isFailure(processResultPayment));
    }

}
