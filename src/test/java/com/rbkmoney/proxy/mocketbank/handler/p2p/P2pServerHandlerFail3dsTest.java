package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.p2p_adapter.Callback;
import com.rbkmoney.damsel.p2p_adapter.CallbackResult;
import com.rbkmoney.damsel.p2p_adapter.Context;
import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.verification.P2pAdapterVerification.isFailure;
import static com.rbkmoney.java.damsel.utils.verification.P2pAdapterVerification.isSleep;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class P2pServerHandlerFail3dsTest extends P2PIntegrationTest {

    @Test
    public void testProcess() throws TException, JsonProcessingException {
        String[] pans = cardList.stream()
                .filter(CardAction::isMpiCardFailed)
                .map(Card::getPan)
                .toArray(String[]::new);

        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            process(cardData);
        }
    }

    private void process(CardData cardData) throws TException, JsonProcessingException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);
        mockMpiVerify(EnrollmentStatus.AUTHENTICATION_AVAILABLE);
        mockMpi(TransactionStatus.AUTHENTICATION_FAILED);

        Context context = createContext(bankCard);
        ProcessResult result = handler.process(context);
        assertTrue("P2P process isn`t sleep", isSleep(result));

        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put("MD", "MD-TAG");
        mapCallback.put("paRes", "SomePaRes");
        context.getSession().setState(result.getNextState());
        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        Callback callback = new Callback();
        callback.setTag(mapCallback.get("MD"));
        callback.setPayload(callbackMap);

        CallbackResult callbackResult = handler.handleCallback(callback, context);
        assertTrue("CallbackResult isn`t failure", isFailure(callbackResult));
    }

}