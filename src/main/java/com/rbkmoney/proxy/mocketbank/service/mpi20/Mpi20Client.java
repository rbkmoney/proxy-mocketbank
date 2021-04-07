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
        String url = mpi20Properties.getThreeDsServerUrl() + "/prepare";
        return restTemplate.postForObject(url, request, PreparationResponse.class);
    }

    public AuthenticationResponse auth(AuthenticationRequest request) {
        String url = mpi20Properties.getThreeDsServerUrl() + "/auth";
        return restTemplate.postForObject(url, request, AuthenticationResponse.class);
    }

    public ResultResponse result(ResultRequest request) {
        String url = mpi20Properties.getThreeDsServerUrl() + "/result";
        return restTemplate.postForObject(url, request, ResultResponse.class);
    }
}
