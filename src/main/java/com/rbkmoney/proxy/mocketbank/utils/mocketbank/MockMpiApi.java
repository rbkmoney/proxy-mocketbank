package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockMpiProperties;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockMpiApi {

    private final RestTemplate restTemplate;
    private final AdapterMockMpiProperties adapterMockMpiProperties;

    public VerifyEnrollmentResponse verifyEnrollment(VerifyEnrollmentRequest request) {
        return sendMessage("verifyEnrollment", request, VerifyEnrollmentResponse.class);
    }

    public ValidatePaResResponse validatePaRes(ValidatePaResRequest request) {
        return sendMessage("validatePaRes", request, ValidatePaResResponse.class);
    }

    private <T> T sendMessage(String methodName, PrepareFieldsObject request, Class<T> responseClass) {
        String prepareUrl = MockMpiUtils.prepareUrl(adapterMockMpiProperties.getUrl(), methodName);
        log.info("MockMpi {} url: {} with request: {}", methodName, prepareUrl, request);
        T response = restTemplate.postForObject(prepareUrl, request.prepareFields(), responseClass);
        log.info("MockMpi {} url: {} with response: {}", methodName, prepareUrl, response);
        return response;
    }

}
