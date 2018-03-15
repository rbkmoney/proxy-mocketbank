package com.rbkmoney.proxy.mocketbank.utils.damsel;


import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.damsel.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


public class ErrorMappingTest {

    @Test(expected = WRuntimeException.class)
    public void testMakeFailureByDescriptionException() {
        ErrorMapping.getFailureByCodeAndDescription(
                "wrong code",
                "wrong description"
        );
    }

    @Test
    public void testMakeFailureByDescription() {
        List<String> list = new ArrayList<>();
        list.add(MocketBankMpiAction.UNSUPPORTED_CARD.getAction());
        list.add(MocketBankMpiAction.THREE_D_SECURE_FAILURE.getAction());
        list.add(MocketBankMpiAction.THREE_D_SECURE_TIMEOUT.getAction());
        list.add(MocketBankMpiAction.INSUFFICIENT_FUNDS.getAction());
        list.add(MocketBankMpiAction.INVALID_CARD.getAction());
        list.add(MocketBankMpiAction.CVV_MATCH_FAIL.getAction());
        list.add(MocketBankMpiAction.EXPIRED_CARD.getAction());
        list.add(MocketBankMpiAction.UNKNOWN_FAILURE.getAction());

        list.forEach(error -> {
            Failure failure = ErrorMapping.getFailureByCodeAndDescription(error, error);
            System.out.println(failure);
        });

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
        failure.setCode("authorization_failed");

        System.out.println(failure);
    }

}