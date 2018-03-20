package com.rbkmoney.proxy.mocketbank.utils.damsel;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ErrorMappingTest {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test(expected = WRuntimeException.class)
    public void testMakeFailureByDescriptionException() {
        ErrorMapping.getFailureByCodeAndDescription(
                "wrong code",
                "wrong description"
        );
    }

    @Test
    public void testMakeFailureByDescription() {

        Map<String, String> map = new HashMap<>();
        map.put(MocketBankMpiAction.UNSUPPORTED_CARD.getAction(), "Failure(code:authorization_failed, reason:'Unsupported Card' - 'Unsupported Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_unsupported))))");

        map.put(MocketBankMpiAction.THREE_D_SECURE_FAILURE.getAction(), "Failure(code:preauthorization_failed, reason:'3-D Secure Failure' - '3-D Secure Failure')");
        map.put(MocketBankMpiAction.THREE_D_SECURE_TIMEOUT.getAction(), "Failure(code:preauthorization_failed, reason:'3-D Secure Timeout' - '3-D Secure Timeout')");
        map.put(MocketBankMpiAction.INSUFFICIENT_FUNDS.getAction(), "Failure(code:authorization_failed, reason:'Insufficient Funds' - 'Insufficient Funds', sub:SubFailure(code:insufficient_funds))");
        map.put(MocketBankMpiAction.INVALID_CARD.getAction(), "Failure(code:authorization_failed, reason:'Invalid Card' - 'Invalid Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_number_invalid))))");
        map.put(MocketBankMpiAction.CVV_MATCH_FAIL.getAction(), "Failure(code:authorization_failed, reason:'CVV Match Fail' - 'CVV Match Fail', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:cvv_invalid))))");
        map.put(MocketBankMpiAction.EXPIRED_CARD.getAction(), "Failure(code:authorization_failed, reason:'Expired Card' - 'Expired Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_expired))))");
        map.put(MocketBankMpiAction.UNKNOWN_FAILURE.getAction(), "Failure(code:authorization_failed, reason:'Unknown Failure' - 'Unknown Failure', sub:SubFailure(code:unknown))");

        map.forEach((k, v) -> {
                    Failure failure = ErrorMapping.getFailureByCodeAndDescription(k, k);
                    logger.info(failure.toString());
                    assertEquals(v, failure.toString());
                }
        );

    }

    @Test
    public void testAttemptsWithMapWithoutWIldCard() {
        Map<String, String> map = new HashMap<>();
        map.put("Unsupported Card", "authorization_failed:payment_tool_rejected:bank_card_rejected:card_unsupported");
        map.put("3-D Secure Failure", "preauthorization_error");
        map.put("3-D Secure Timeout", "preauthorization_error");
        map.put("Invalid Card", "authorization_failed:payment_tool_rejected:bank_card_rejected:card_number_invalid");
        map.put("CVV Match Fail", "authorization_failed:payment_tool_rejected:bank_card_rejected:cvv_invalid");
        map.put("Expired Card", "authorization_failed:payment_tool_rejected:bank_card_rejected:card_expired");
        map.put("Unknown", "authorization_failed:unknown");
        map.put("Unknown Failure", "authorization_failed:unknown");

        String code = "Unsupported Card";
        String type = map.entrySet().stream()
                .filter(m -> m.getKey().contains(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getValue();

        Failure failure = toGeneral(type);

        logger.info(failure.toString());
        assertEquals("Failure(code:authorization_failed, sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_unsupported))))", failure.toString());
    }

}