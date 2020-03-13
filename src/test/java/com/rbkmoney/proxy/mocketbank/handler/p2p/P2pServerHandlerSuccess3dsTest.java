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
import com.rbkmoney.proxy.mocketbank.utils.CardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.verification.P2pAdapterVerification.isSleep;
import static com.rbkmoney.java.damsel.utils.verification.P2pAdapterVerification.isSuccess;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class P2pServerHandlerSuccess3dsTest extends P2PIntegrationTest {

    @Test
    public void testProcessSuccess3ds() throws TException, JsonProcessingException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isMpiCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            process(cardData);
        }
    }

    private void process(CardData cardData) throws TException, JsonProcessingException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);
        mockMpiVerify(EnrollmentStatus.AUTHENTICATION_AVAILABLE);
        mockMpi(TransactionStatus.AUTHENTICATION_SUCCESSFUL);

        Context context = createContext(bankCard);
        ProcessResult result = handler.process(context);
        assertTrue("P2P process isn`t sleep", isSleep(result));

        Map<String, String> payload = new HashMap<>();
        payload.put("MD", "MD-TAG");
        payload.put("paRes", "SomePaRes");

        context.getSession().setState(result.getNextState());
        Callback callback = prepareCallback(payload);

        CallbackResult callbackResult = handler.handleCallback(callback, context);
        assertTrue("CallbackResult isn`t success", isSuccess(callbackResult));
    }

}