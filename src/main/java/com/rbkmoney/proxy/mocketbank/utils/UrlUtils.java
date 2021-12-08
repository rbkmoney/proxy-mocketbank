package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.MpiField;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

    public static String getCallbackUrl(InvoicePayment payment, String callbackUrl, String path) {
        if (payment != null && payment.isSetPayerSessionInfo()
                && StringUtils.hasText(payment.getPayerSessionInfo().getRedirectUrl())) {
            return payment.getPayerSessionInfo().getRedirectUrl();
        }

        return getCallbackUrl(callbackUrl, path);
    }

    public static String getCallbackUrl(String callbackUrl, String path) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .build()
                .toUriString();
    }

    public static String getCallbackUrl(String callbackUrl, String path, MultiValueMap<String, String> params) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .queryParams(params)
                .build()
                .toUriString();
    }

    public static Map<String, String> prepareRedirectParams(
            VerifyEnrollmentResponse verifyEnrollmentResponse, String tag, String termUrl
    ) {
        Map<String, String> params = new HashMap<>();
        params.put(MpiField.PA_REQ.getValue(), verifyEnrollmentResponse.getPaReq());
        params.put(MpiField.MD.getValue(), tag);
        params.put(MpiField.TERM_URL.getValue(), termUrl);
        return params;
    }

}
