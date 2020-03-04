package com.rbkmoney.proxy.mocketbank.utils.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.MpiField;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StateUtils {

    public static byte[] prepareState(VerifyEnrollmentResponse verifyEnrollmentResponse) {
        Map<String, String> extra = new HashMap<>();
        extra.put(MpiField.PA_REQ.getValue(), verifyEnrollmentResponse.getPaReq());
        try {
            return Converter.mapToByteArray(extra);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
