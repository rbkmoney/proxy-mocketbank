package com.rbkmoney.proxy.mocketbank.service.mpi20;

import com.rbkmoney.proxy.mocketbank.configuration.properties.Mpi20Properties;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class Mpi20Client {

    private final RestTemplate restTemplate;
    private final Mpi20Properties mpi20Properties;

    public PreparationResponse prepare(PreparationRequest request) {
        return send("prepare", request, PreparationResponse.class);
    }

    public AuthenticationResponse auth(AuthenticationRequest request) {
        return send("auth", request, AuthenticationResponse.class);
    }

    public ResultResponse result(ResultRequest request) {
        return send("result", request, ResultResponse.class);
    }

    private <T, N> T send(String methodName, N request, Class<T> responseClass) {
        String prepareUrl = prepareUrl(methodName);
        log.info("MockV2Mpi {} url: {} with request: {}", methodName, prepareUrl, request);
        T response = restTemplate.postForObject(prepareUrl, request, responseClass);
        log.info("MockV2Mpi {} url: {} with response: {}", methodName, prepareUrl, response);
        return response;
    }

    private String prepareUrl(String path) {
        return String.format("%s/mpi20/%s", mpi20Properties.getThreeDsServerUrl(), path);
    }
}
