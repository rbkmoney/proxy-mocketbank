package com.rbkmoney.proxy.mocketbank.utils.damsel;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiAction;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;
import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ErrorMappingTest {

    @Autowired
    private ErrorMapping errorMapping;

    @Test(expected = WRuntimeException.class)
    public void testMakeFailureByDescriptionException() {
        errorMapping.getFailureByCodeAndDescription(
                "wrong code",
                "wrong description"
        );
    }

    @Test
    public void testMakeFailureByDescription() {

        Map<String, String> map = new HashMap<>();
        map.put(MpiAction.UNSUPPORTED_CARD.getAction(), "Failure(code:authorization_failed, reason:'Unsupported Card' - 'Unsupported Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_unsupported))))");

        map.put(MpiAction.THREE_D_SECURE_FAILURE.getAction(), "Failure(code:preauthorization_failed, reason:'3-D Secure Failure' - '3-D Secure Failure')");
        map.put(MpiAction.THREE_D_SECURE_TIMEOUT.getAction(), "Failure(code:preauthorization_failed, reason:'3-D Secure Timeout' - '3-D Secure Timeout')");
        map.put(MpiAction.INSUFFICIENT_FUNDS.getAction(), "Failure(code:authorization_failed, reason:'Insufficient Funds' - 'Insufficient Funds', sub:SubFailure(code:insufficient_funds))");
        map.put(MpiAction.INVALID_CARD.getAction(), "Failure(code:authorization_failed, reason:'Invalid Card' - 'Invalid Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_number_invalid))))");
        map.put(MpiAction.CVV_MATCH_FAIL.getAction(), "Failure(code:authorization_failed, reason:'CVV Match Fail' - 'CVV Match Fail', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:cvv_invalid))))");
        map.put(MpiAction.EXPIRED_CARD.getAction(), "Failure(code:authorization_failed, reason:'Expired Card' - 'Expired Card', sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_expired))))");
        map.put(MpiAction.UNKNOWN_FAILURE.getAction(), "Failure(code:authorization_failed, reason:'Unknown Failure' - 'Unknown Failure', sub:SubFailure(code:unknown))");

        map.forEach((k, v) -> {
                    Failure failure = errorMapping.getFailureByCodeAndDescription(k, k);
                    log.info(failure.toString());
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

        log.info(failure.toString());
        assertEquals("Failure(code:authorization_failed, sub:SubFailure(code:payment_tool_rejected, sub:SubFailure(code:bank_card_rejected, sub:SubFailure(code:card_unsupported))))", failure.toString());
    }

}