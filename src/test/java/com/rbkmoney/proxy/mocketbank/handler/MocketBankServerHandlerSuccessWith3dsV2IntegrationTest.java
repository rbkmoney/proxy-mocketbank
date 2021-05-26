package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.CardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetCaptured;
import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuspend;
import static com.rbkmoney.proxy.mocketbank.TestData.DEFAULT_THREE_METHOD_DATA;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static com.rbkmoney.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields.CREQ;
import static com.rbkmoney.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields.THREE_DS_METHOD_DATA;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:8018",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerSuccessWith3dsV2IntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentSuccess() throws TException, IOException, JSONException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardEnrolled20);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException, IOException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);
        mockMpiV2Prepare();
        mockMpiV2Auth();
        mockMpiV2Result();

        PaymentContext paymentContext = getContext(bankCard, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process payment isn`t suspend", isSuspend(proxyResult));

        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put(THREE_DS_METHOD_DATA, DEFAULT_THREE_METHOD_DATA);
        paymentContext.getSession().setState(proxyResult.getNextState());
        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        PaymentCallbackResult callbackPrepareResult = handler.handlePaymentCallback(callbackMap, paymentContext);
        assertTrue("CallbackResult isn`t success",
                callbackPrepareResult.getResult().getIntent().isSetSuspend());

        paymentContext.getSession().setState(callbackPrepareResult.getResult().getNextState());
        Map<String, String> mapCallback2 = new HashMap<>();
        ByteBuffer callbackMap2 = Converter.mapToByteBuffer(mapCallback2);
        mapCallback2.put(CREQ, CREQ);
        PaymentCallbackResult callbackAuthResult = handler.handlePaymentCallback(callbackMap2, paymentContext);
        assertTrue("CallbackResult isn`t success", isSuccess(callbackAuthResult));

        paymentContext.getSession().setTarget(createTargetCaptured());
        paymentContext.getSession().setState(callbackAuthResult.getResult().getNextState());
        paymentContext.getPaymentInfo().getPayment().setTrx(callbackAuthResult.getResult().getTrx());
        PaymentProxyResult processResultCapture = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success", isSuccess(processResultCapture));
    }

}
