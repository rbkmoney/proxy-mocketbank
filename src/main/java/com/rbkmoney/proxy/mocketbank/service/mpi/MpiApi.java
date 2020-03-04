package com.rbkmoney.proxy.mocketbank.service.mpi;

import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockMpiProperties;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpiApi {

    private final RestTemplate restTemplate;
    private final AdapterMockMpiProperties adapterMockMpiProperties;

    public VerifyEnrollmentResponse verifyEnrollment(CardDataProxyModel cardData) {
        VerifyEnrollmentRequest request = VerifyEnrollmentRequest.builder()
                .pan(cardData.getPan())
                .year(cardData.getExpYear())
                .month(cardData.getExpMonth())
                .build();
        return verifyEnrollment(request);
    }

    public VerifyEnrollmentResponse verifyEnrollment(VerifyEnrollmentRequest request) {
        return sendMessage("verifyEnrollment", request, VerifyEnrollmentResponse.class);
    }

    public ValidatePaResResponse validatePaRes(CardDataProxyModel cardData, Map<String, String> options) {
        ValidatePaResRequest request = ValidatePaResRequest.builder()
                .pan(cardData.getPan())
                .paRes(options.get("paRes"))
                .build();
        return validatePaRes(request);
    }

    public ValidatePaResResponse validatePaRes(ValidatePaResRequest request) {
        return sendMessage("validatePaRes", request, ValidatePaResResponse.class);
    }

    private <T> T sendMessage(String methodName, PrepareFieldsObject request, Class<T> responseClass) {
        String prepareUrl = prepareUrl(adapterMockMpiProperties.getUrl(), methodName);
        log.info("MockMpi {} url: {} with request: {}", methodName, prepareUrl, request);
        T response = restTemplate.postForObject(prepareUrl, request.prepareFields(), responseClass);
        log.info("MockMpi {} url: {} with response: {}", methodName, prepareUrl, response);
        return response;
    }

    private String prepareUrl(String url, String path) {
        return String.format("%s/mpi/%s", url, path);
    }

}
