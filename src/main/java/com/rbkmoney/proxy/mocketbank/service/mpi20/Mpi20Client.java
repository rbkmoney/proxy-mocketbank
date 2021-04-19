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
        return restTemplate.postForObject(prepareUrl("prepare"), request, PreparationResponse.class);
    }

    public AuthenticationResponse auth(AuthenticationRequest request) {
        return restTemplate.postForObject(prepareUrl("auth"), request, AuthenticationResponse.class);
    }

    public ResultResponse result(ResultRequest request) {
        return restTemplate.postForObject(prepareUrl("result"), request, ResultResponse.class);
    }

    private String prepareUrl(String path) {
        return String.format("%s/mpi20/%s", mpi20Properties.getThreeDsServerUrl(), path);
    }
}
