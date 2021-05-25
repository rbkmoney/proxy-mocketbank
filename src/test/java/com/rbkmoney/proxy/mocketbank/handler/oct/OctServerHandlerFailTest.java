package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.PayoutCardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayoutAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.identity-document-storage.url=http://127.0.0.1:8021/v1/identity_document_storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerFailTest extends OctIntegrationTest {

    @Test
    void testProcessWithdrawal() throws TException {
        List<String> pans = PayoutCardListUtils.extractPans(cardPayoutList, CardPayoutAction::isCardFailed);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processWithdrawalFail(cardData);
        }
    }

    private void processWithdrawalFail(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);

        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(bankCard),
                Value.str(""),
                createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue("Result processWithdrawal isn`t success", isFailure(result));
    }

    public static boolean isFailure(ProcessResult processResult) {
        return processResult.getIntent().getFinish().getStatus().isSetFailure();
    }

}
