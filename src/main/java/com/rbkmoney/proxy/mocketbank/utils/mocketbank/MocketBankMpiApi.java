package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class MocketBankMpiApi {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${proxy-mocketbank-mpi.url}")
    private String url;

    public VerifyEnrollmentResponse verifyEnrollment(String pan, short year, byte month) throws IOException {
        log.info("VerifyEnrollment input params: pan {}, year {}, month {}",
                MocketBankMpiUtils.maskNumber(pan), year, month
        );

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("pan", pan);
        map.add("year", String.valueOf(year));
        map.add("month", String.valueOf(month));

        VerifyEnrollmentResponse response = restTemplate.postForObject(prepareUrl("verifyEnrollment"), map, VerifyEnrollmentResponse.class);

        log.info("VerifyEnrollment response {}", response);
        return response;
    }

    public ValidatePaResResponse validatePaRes(String pan, String paRes) throws IOException {
        log.info("ValidatePaRes input params: pan {}, paRes {}", MocketBankMpiUtils.maskNumber(pan), paRes);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("pan", pan);
        map.add("paRes", paRes);

        ValidatePaResResponse response = restTemplate.postForObject(prepareUrl("validatePaRes"), map, ValidatePaResResponse.class);
        log.info("ValidatePaRes response {}", response);
        return response;
    }

    private String prepareUrl(String path) {
        return String.format("%s/mpi/%s", url, path);
    }

}
