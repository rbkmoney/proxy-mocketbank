package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
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

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetCaptured;
import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuspend;
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
public class MocketBankServerHandlerRecurrent3DSSuccessIntegrationTest extends IntegrationTest {

    @Test
    public void testProcessPaymentSuccess() throws TException, IOException {
        String[] pans = cardList.stream()
                .filter(CardAction::isMpiCardSuccess)
                .map(Card::getPan)
                .toArray(String[]::new);

        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException, IOException {
        BankCard bankCard = TestData.createBankCard(cardData);
        bankCard.setToken(TestData.BANK_CARD_TOKEN);
        mockCds(cardData, bankCard);
        mockMpiVerify(EnrollmentStatus.AUTHENTICATION_AVAILABLE);
        mockMpi(TransactionStatus.AUTHENTICATION_SUCCESSFUL);

        RecurrentTokenContext context = createRecurrentTokenContext(bankCard);
        RecurrentTokenProxyResult tokenProxyResult = handler.generateToken(context);
        assertTrue("GenerateToken isn`t suspend", isSuspend(tokenProxyResult));

        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put("MD", "MD-TAG");
        mapCallback.put("paRes", "SomePaRes");
        context.getSession().setState(tokenProxyResult.getNextState());
        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        RecurrentTokenCallbackResult tokenCallbackResult = handler.handleRecurrentTokenCallback(callbackMap, context);
        assertTrue("HandleRecurrentTokenCallback isn`t success", isSuccess(tokenCallbackResult));

        // process
        String token = tokenCallbackResult.getResult().getIntent().getFinish().getStatus().getSuccess().getToken();

        PaymentContext paymentContext = getContext(getPaymentResourceRecurrent(token), createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process payment isn`t success", isSuccess(proxyResult));

        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        paymentContext.getSession().setTarget(createTargetCaptured());

        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Capture isn`t success", isSuccess(proxyResult));
    }

}
