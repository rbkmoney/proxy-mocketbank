package com.rbkmoney.proxy.test.utils.testmpi;

import com.rbkmoney.proxy.test.utils.testmpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.test.utils.testmpi.model.VerifyEnrollmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class TestMpiApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestMpiApi.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${proxy-test-mpi.url}")
    private String url;

    public VerifyEnrollmentResponse verifyEnrollment(String pan, short year, byte month) throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("pan", pan);
        map.add("year", String.valueOf(year));
        map.add("month", String.valueOf(month));

        VerifyEnrollmentResponse result;
        result = restTemplate.postForObject(prepareUrl("verifyEnrollment"), map, VerifyEnrollmentResponse.class);
        return result;
    }

    public ValidatePaResResponse validatePaRes(String pan, String paRes) throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("pan", pan);
        map.add("paRes", String.valueOf(paRes));

        ValidatePaResResponse result;
        result = restTemplate.postForObject(prepareUrl("validatePaRes"), map, ValidatePaResResponse.class);
        return result;
    }

    private String prepareUrl(String path) throws IOException {
        return url + "/mpi/" + path;
    }

}
